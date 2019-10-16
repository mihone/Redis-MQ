package com.github.mihone.redismq.mq;

import com.github.mihone.redismq.annotation.EnableRedisMQMonitor;
import com.github.mihone.redismq.annotation.Queue;
import com.github.mihone.redismq.cache.Cache;
import com.github.mihone.redismq.config.BasicConfig;
import com.github.mihone.redismq.config.RedisMqConfig;
import com.github.mihone.redismq.interceptor.MonitorInterceptor;
import com.github.mihone.redismq.json.JsonUtils;
import com.github.mihone.redismq.log.Log;
import com.github.mihone.redismq.redis.RedisUtils;
import com.github.mihone.redismq.reflect.ClassUtils;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>Redis-MQ starter.
 * <p>you should use {@code RedisMQ.start()}  to start the mq,
 * else a bean provider and the main class is required.
 * when started,some queue listener thread will be created to listen the queue。
 * Also,a queue listener check thread will be created to check queue listener is working or not.
 * <p>{@code @EnableRedisMQMonitor} is a symbol to start monitor. The monitor has the following features:
 * <p> Check delay messages which are ready to send.
 * <p> Resend the message in the dead queue.
 * <p> Resend the unack message.when consumer has acquired the message but some errors happened and the consumer did not ack the mq.The monitor will check the timeout message and resend it .
 * The monitor can run alone.Therefore,it is necessary to definite queue name and delay queue name in the application.yml/application.properties used {@code redis.mq.queues} and {@code redis.mq.delayQueues}.
 * Values of those two keys are array and separate by ",".
 * <p>Note that target method will be invoke in the queue listener thread.
 * Therefore, it will never throw an exception but write a log and print its stacktrace.
 * All wrongs in the child threads are basic logs.
 * However,Redis-MQ just defined logger with slf4j but has not any implemention.
 * So you must use logback or log4j or other implementions to get the log info.
 *
 * @author mihone
 * @since 2019/10/5
 */
public final class RedisMQ {

    private static Integer threadSize;
    private static Function beanProvider;
    private static final Log log = Log.getLogger(RedisMQ.class);

    public static <R, T> void start(Function<Class, R> beanProvider, Class<T> clazz) {
        if (beanProvider == null || clazz == null) {
            throw new IllegalArgumentException("illegal arguments");
        }
        log.info("redismq is starting...");
        RedisMQ.beanProvider = beanProvider;


        Map<Runnable, Future> queueListeners = new HashMap<>();
        Map<String, Runnable> monitorNames = new HashMap<>();
        Map<Runnable, Future> monitors = new HashMap<>();

        List<Class<?>> classes = ClassUtils.getAllClasses(clazz);
        init(classes);
        List<Method> methodList = classes.parallelStream().filter(ClassUtils::isRealClass).flatMap(c -> Arrays.stream(c.getMethods())).filter(m -> m.getAnnotation(Queue.class) != null).collect(Collectors.toList());
        if (methodList.size() == 0) {
            threadSize=1;
            log.info("there is no queue needs to listen");
        } else {
            threadSize = methodList.size();
            for (Method method : methodList) {
                Queue queue = method.getAnnotation(Queue.class);
                String queueName = queue.value();
                Cache.writeToMethodCache(queueName, method);
                Runnable task = () -> {
                    Jedis jedis = RedisMQInitializer.getJedis();
                    jedis.subscribe(new ConsumeHandler(), queueName);
                };
                Future<?> future = RedisMQInitializer.fixedThreadPool.submit(task);
                queueListeners.put(task, future);
            }
            log.info("queue listeners is created");
        }

        EnableRedisMQMonitor monitor = clazz.getAnnotation(EnableRedisMQMonitor.class);
        if (monitor != null) {
            String[] delayQueues = RedisMqConfig.getDelayQueues();
            MonitorInterceptor monitorInterceptor = (MonitorInterceptor)Cache.getFromBeanCache("MonitorInterceptor");
            Runnable delayQueueTask = () -> {
                Jedis jedis = RedisUtils.getJedis();
                try {
                    if (!monitorInterceptor.delay(jedis)) {
                        return;
                    }
                    for (String delayQueue : delayQueues) {
                        String currentTime = String.valueOf(System.currentTimeMillis());
                        Set<byte[]> messages = jedis.zrangeByScore((delayQueue + BasicConfig.DELAY_QUEUE_SUFFIX).getBytes(), "-inf".getBytes(), currentTime.getBytes());
                        messages.parallelStream().forEach(msg -> MqUtils.send(delayQueue, msg, JsonUtils.convertObjectFromBytes(msg, Message.class).getMessageId()));
                        jedis.zremrangeByScore(delayQueue + BasicConfig.DELAY_QUEUE_SUFFIX, "-inf", currentTime);
                    }
                } finally {
                    jedis.close();
                }
            };
            ScheduledFuture<?> delayQueueFuture = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(delayQueueTask, 0, RedisMqConfig.getDelayCheckInterval(), TimeUnit.MILLISECONDS);
            monitorNames.put(BasicConfig.DELAY_QUEUE_SUFFIX, delayQueueTask);
            monitors.put(delayQueueTask, delayQueueFuture);
            log.info("delay queue monitor is created....");

            Runnable deadMessageTask = () -> {
                String[] queues = RedisMqConfig.getQueues();
                Jedis jedis = RedisUtils.getJedis();
                try {
                    if (!monitorInterceptor.dead(jedis)) {
                        return;
                    }
                    Arrays.stream(queues).forEach(queue -> {
                        int count = Integer.parseInt(jedis.pubsubNumSub(queue).get(queue));
                        if (count > 0) {
                            List<byte[]> deadMessages = jedis.lrange((queue + BasicConfig.DEAD_QUEUE_SUFFIX).getBytes(), 0, -1);
                            deadMessages.parallelStream().forEach(msg -> MqUtils.send(queue, msg, JsonUtils.convertObjectFromBytes(msg, Message.class).getMessageId()));
                            jedis.ltrim(queue + BasicConfig.DEAD_QUEUE_SUFFIX, deadMessages.size(), -1);
                        }
                    });
                } finally {
                    jedis.close();
                }

            };
            ScheduledFuture<?> deadMessageFuture = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(deadMessageTask, 30000, RedisMqConfig.getDeadCheckInterval(), TimeUnit.MILLISECONDS);
            monitorNames.put(BasicConfig.DEAD_QUEUE_SUFFIX, deadMessageTask);
            monitors.put(deadMessageTask, deadMessageFuture);
            log.info("dead message monitor thread  is created...");


            Runnable backMessageTask = () -> {
                String[] queues = RedisMqConfig.getQueues();
                Jedis jedis = RedisUtils.getJedis();
                try {
                    if (!monitorInterceptor.back(jedis)) {
                        return;
                    }
                    Arrays.stream(queues).forEach(queue -> {
                        List<byte[]> backMessages = jedis.lrange((queue + BasicConfig.BACK_QUEUE_SUFFIX).getBytes(), 0, -1);
                        backMessages.parallelStream().forEach(msg -> {
                            Message message = JsonUtils.convertObjectFromBytes(msg, Message.class);
                            long timeStamp = message.getTimeStamp();
                            if (System.currentTimeMillis() - timeStamp > 5 * 60 * 1000) {
                                MqUtils.send(queue, msg, message.getMessageId());
                            }
                            jedis.lrem((queue + BasicConfig.BACK_QUEUE_SUFFIX).getBytes(), 0, msg);
                        });

                    });
                } finally {
                    jedis.close();
                }
            };
            ScheduledFuture<?> backMessageFuture = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(backMessageTask, 0, RedisMqConfig.getBackCheckInterval(), TimeUnit.MILLISECONDS);
            monitorNames.put(BasicConfig.BACK_QUEUE_SUFFIX, backMessageTask);
            monitors.put(backMessageTask, backMessageFuture);
            log.info("Back queue message monitor is created...");
        }

        RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(() -> {
            queueListeners.entrySet().forEach(entry -> {
                if (entry.getValue().isDone()) {
                    Future future = RedisMQInitializer.fixedThreadPool.submit(entry.getKey());
                    entry.setValue(future);
                }
            });
            monitorNames.forEach((key, value) -> {
                if (monitors.get(value).isDone()) {
                    switch (key) {
                        case BasicConfig.BACK_QUEUE_SUFFIX: {
                            ScheduledFuture<?> future = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(value, 0, RedisMqConfig.getBackCheckInterval(), TimeUnit.MILLISECONDS);
                            monitors.put(value, future);
                            break;
                        }
                        case BasicConfig.DEAD_QUEUE_SUFFIX: {
                            ScheduledFuture<?> future = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(value, 0, RedisMqConfig.getDeadCheckInterval(), TimeUnit.MILLISECONDS);
                            monitors.put(value, future);
                            break;
                        }
                        case BasicConfig.DELAY_QUEUE_SUFFIX: {
                            ScheduledFuture<?> future = RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(value, 0, RedisMqConfig.getDelayCheckInterval(), TimeUnit.MILLISECONDS);
                            monitors.put(value, future);
                            break;
                        }
                    }
                }
            });
        }, 60, 60, TimeUnit.SECONDS);

        log.info("check queue listener status thread  is created...");
        log.info("redismq is started...");
    }

    private static void init(List<Class<?>> classes) {
        List<Class<?>> interceptor = classes.stream().filter(clazz -> MonitorInterceptor.class.isAssignableFrom(clazz)).collect(Collectors.toList());
        if (interceptor.size()>1) {
            throw new IllegalArgumentException("Implemention of MonitorInterceptor can be only one ");
        }else if(interceptor.size()==0){
            Cache.writeToBeanCache("MonitorInterceptor",new MonitorInterceptor(){});
        }
        try {
            Cache.writeToBeanCache("MonitorInterceptor",interceptor.get(0).newInstance());
        } catch (InstantiationException |IllegalAccessException e) {
           log.error("get instance of interceptor error..Cause:",e);
        }

    }

    public static Function getBeanProvider() {
        return beanProvider;
    }

    private static class RedisMQInitializer {
        private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadSize);
        private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(4);

        private static Jedis getJedis() {
            try {
                Jedis jedis = new Jedis(RedisUtils.getUrl(), RedisUtils.getPort());
                jedis.auth(RedisUtils.getPassword());
                return jedis;
            } catch (Exception e) {
                log.error("can not get jedis!Cause:{}", e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    log.error("interrupted when sleeping to reget jedis instance.Cause:{}", ex);
                    return getJedis();
                }
                return getJedis();
            }
        }
    }


}
