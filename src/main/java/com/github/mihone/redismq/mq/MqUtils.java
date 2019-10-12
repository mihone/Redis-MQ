package com.github.mihone.redismq.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.mihone.redismq.config.BasicConfig;
import com.github.mihone.redismq.config.RedisMqConfig;
import com.github.mihone.redismq.json.JsonUtils;
import com.github.mihone.redismq.log.Log;
import com.github.mihone.redismq.redis.RedisUtils;
import com.github.mihone.redismq.thread.ThreadUtils;
import com.github.mihone.redismq.exception.ClassResolveFailedException;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Send and receive message utils
 *
 * @author mihone
 * @since 2019/10/6
 */
public final class MqUtils {
    private static final Log log = Log.getLogger(MqUtils.class);

    /**
     * @param queue the name of queue
     * @param data  data which want to send
     * @throws IllegalArgumentException param can not be null
     * @author mihone
     * @since 2019/10/6
     */
    public static void sendMessage(final String queue, final Object data) {
        if (data == null) {
            throw new IllegalArgumentException("message data can not be null");
        }
        if (queue == null) {
            throw new IllegalArgumentException("queue name can not be null");
        }
        String messageId = String.valueOf(IdGenerator.generateId());
        long timeStamp = System.currentTimeMillis();
        String className = data.getClass().getName();
        Message message = new Message(messageId,className,timeStamp,data);
        final byte[] msgBytes = JsonUtils.convertToBytes(message);
        if (msgBytes == null) {
            return;
        }
        send(queue, msgBytes, messageId);

    }
    public static void sendDelayMessage(final String queue, final Object data, final long delay, final TimeUnit unit){
        String messageId = String.valueOf(IdGenerator.generateId());
        long timeStamp = System.currentTimeMillis();
        String className = data.getClass().getName();
        Message message = new Message(messageId,className,timeStamp,data);
        long delayMills = unit.toMillis(delay);
        Jedis jedis = RedisUtils.getJedis();
        final byte[] msgBytes = JsonUtils.convertToBytes(message);
        if (msgBytes == null) {
            return;
        }
        jedis.zadd(queue.getBytes(), timeStamp+delayMills, msgBytes);
    }


    /**
     * @param queue name of queue
     * @return body of {@link Message}. Null if queue has not messages
     * @throws IllegalArgumentException param can not be null
     * @author mihone
     * @since 2019/10/6
     */
    public static Object receiveMessage(String queue) {
        if (queue == null) {
            throw new IllegalArgumentException("queue name can not be null");
        }
        Jedis jedis = RedisUtils.getJedis();
        Map<String, String> pubsubNumSub = jedis.pubsubNumSub(queue);
        int count = Integer.parseInt(pubsubNumSub.get(queue));
        if (count > 0) {
            return null;
        } else {
            byte[] msg = jedis.rpoplpush((queue + BasicConfig.DEAD_QUEUE_SUFFIX).getBytes(), (queue + BasicConfig.BACK_QUEUE_SUFFIX).getBytes());
            if (msg == null) {
                return null;
            }
            Message message = JsonUtils.convertObjectFromBytes(msg, Message.class);
            String className = message.getClassName();
            try {
                Object result = JsonUtils.convertObjectFromBytes(JsonUtils.convertToBytes(message.getBody()),Class.forName(className));
                jedis.lrem((queue + BasicConfig.BACK_QUEUE_SUFFIX).getBytes(),0,msg);
                return result;
            } catch (ClassNotFoundException e) {
                log.error("can not resolve class.Class:{},Cause:{}", className, e);
                throw new ClassResolveFailedException("can not resolve class.");
            } finally {
                jedis.close();
            }
        }
    }

    static void send(String queue, byte[] msgBytes, String messageId) {
        ThreadUtils.submit(() -> {
            Jedis jedis = RedisUtils.getJedis();
            Long result = jedis.lpush(queue.getBytes(), msgBytes);
            Map<String, String> pubsubNumSub = jedis.pubsubNumSub(queue);
            if (Integer.parseInt(pubsubNumSub.get(queue)) == 0) {
                jedis.lrem(queue.getBytes(), 0, msgBytes);
                jedis.lpush((queue + BasicConfig.DEAD_QUEUE_SUFFIX).getBytes(), msgBytes);
                jedis.close();
                return;
            }
            int count = 0;
            while (count <= RedisMqConfig.getTimeout()) {
                long reply = jedis.publish(queue, messageId);
                log.info("reply：" + reply);
                try {
                    if (reply == 0) {
                        count++;
                        Thread.sleep(1000);
                    } else {
                        jedis.close();
                        return;
                    }
                } catch (InterruptedException e) {
                    log.error("sleep state is interrupted.Cause:{}", e);
                    break;
                }
            }
            jedis.lrem(queue.getBytes(), 0, msgBytes);
            jedis.lpush((queue + BasicConfig.DEAD_QUEUE_SUFFIX).getBytes(), msgBytes);
            jedis.close();
        });
    }

    private MqUtils() {
    }
}
