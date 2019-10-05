package com.github.mihone.redismq.mq;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mihone.redismq.cache.Cache;
import com.github.mihone.redismq.json.JsonUtils;
import com.github.mihone.redismq.log.Log;
import com.github.mihone.redismq.redis.ConsumeHandler;
import com.github.mihone.redismq.redis.RedisUtils;
import com.github.mihone.redismq.reflect.MethodInvocationHandler;
import com.github.mihone.redismq.annotation.Queue;
import com.github.mihone.redismq.reflect.ClassUtils;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>redismq starter.<p/>
 * you should use <code>RedisMQ.start()</code>  to start the mq,</br>
 * alse a bean provider and the main class is required.</br>
 * when started,some queue listener thread will be created,and three schedular thread will be created.</br>
 * <p>First is to check queue listener thread is working or not .if not recreated it.</p>
 * <p>Second is to resend the message in the dead queue.</p>
 * <p>Third is to resend the unack message.when consumer has acquired the message but some errors happened and the consumer did not ack the mq.
 * This thread will check the timeout message and resend it .</p>
 *<p>Note that target method will be invoke in the queue listener thread.
 * Therefore, it will never throw an exception but write a log and print its stacktrace.
 * All wrongs in the child threads are basic logs.
 * However,redismq just defined logger with slf4j but has not any implemention.
 * So you must use logback or log4j or other implemetions to get the log info.</p>
 * @author mihone
 * @date 2019/10/5
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

        List<Class<?>> classes = ClassUtils.getAllClasses(clazz);
        List<Method> methodList = classes.parallelStream().filter(ClassUtils::isRealClass).flatMap(c -> Arrays.stream(c.getMethods())).filter(m -> m.getAnnotation(Queue.class) != null).collect(Collectors.toList());
        if (methodList.size() == 0) {
            log.info("there is no queue needs to listen");
            return;
        }
        threadSize = methodList.size();
        for (Method method : methodList) {
            Queue queue = method.getAnnotation(Queue.class);
            String queueName = queue.value();
            Cache.writeToMethodCache(queueName, method);
            RedisMQInitializer.fixedThreadPool.submit(() -> {
                Jedis jedis = RedisMQInitializer.getJedis();
                jedis.subscribe(new ConsumeHandler(), queueName);
            });
        }
        log.info("queue listeners is created");
        RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(() -> {
            Jedis jedis = RedisUtils.getJedis();
            List<String> activeChannels = jedis.pubsubChannels("*");
            List<String> collect = activeChannels.stream().filter(channel -> Cache.getFromMethodCache(channel) != null).collect(Collectors.toList());
            if (collect.size() > 0) {
                collect.forEach(channel -> RedisMQInitializer.fixedThreadPool.submit(() -> {
                    Jedis j = RedisMQInitializer.getJedis();
                    jedis.subscribe(new ConsumeHandler(), channel);
                }));
            }
        }, 10, 30, TimeUnit.SECONDS);
        log.info("check queue listener status thread  is created...");
        RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(() -> {
            List<String> keys = Cache.getCurrentCacheKeysFromMethodCache();
            keys.parallelStream().forEach(key -> MethodInvocationHandler.handler(key));
        }, 30, 30, TimeUnit.SECONDS);
        log.info("dead message handler thread  is created...");
        RedisMQInitializer.scheduledThreadPool.scheduleAtFixedRate(() -> {
            Jedis jedis = RedisUtils.getJedis();
            List<String> keys = Cache.getCurrentCacheKeysFromMethodCache();
            keys.stream().forEach(key -> {
                List<byte[]> backList = jedis.lrange(key.getBytes(), 0, -1);
                backList.forEach(msg -> {
                    ObjectNode read = JsonUtils.read(msg);
                    if (null != read) {
                        String messageId = read.get("timestamp").toString();
                        long timeStamp = Long.parseLong(messageId);
                        if (System.currentTimeMillis() - timeStamp > 5 * 60 * 1000) {
                            MqUtils.send(key, msg, messageId);
                        }
                    }
                });
            });
        }, 60, 60, TimeUnit.SECONDS);
        log.info("redismq is started...");
    }

    public static Function getBeanProvider() {
        return beanProvider;
    }

    private static class RedisMQInitializer {
        private static final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadSize);
        private static final ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(3);

        private static Jedis getJedis() {
            try {
                Jedis jedis  = new Jedis(RedisUtils.getUrl(), RedisUtils.getPort());
                jedis.auth(RedisUtils.getPassword());
                return jedis;
            } catch (Exception e) {
               log.error("can not get jedis!Cause:{}",e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    log.error("interrupted when sleeping to reget jedis instance.Cause:{}",ex);
                    return getJedis();
                }
                return getJedis();
            }
        }
    }


}
