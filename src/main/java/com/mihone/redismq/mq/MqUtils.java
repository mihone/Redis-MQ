package com.mihone.redismq.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mihone.redismq.config.BasicConfig;
import com.mihone.redismq.config.RedisMqConfig;
import com.mihone.redismq.exception.ClassResolveFailedException;
import com.mihone.redismq.json.JsonUtils;
import com.mihone.redismq.log.Log;
import com.mihone.redismq.redis.RedisUtils;
import com.mihone.redismq.thread.ThreadUtils;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public final class MqUtils {
    private static final RedisMqConfig REDIS_MQ_CONFIG = new RedisMqConfig();
    private static final Log log = Log.getLogger(MqUtils.class);

    public static void sendMessage(final String queue, final Object data) {
        if (data == null) {
            throw new IllegalArgumentException("message data can not be null");
        }
        if (queue == null) {
            throw new IllegalArgumentException("queue name can not be null");
        }
        String messageId = String.valueOf(IdGenerator.generateId());
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String className = data.getClass().getName();
        HashMap<String, Object> props = new HashMap<>();
        props.put("className", className);
        props.put("timestamp", timeStamp);
        props.put("id", messageId);
        JsonNode message = JsonUtils.addAttributes(data, props);
        final byte[] msgBytes = JsonUtils.convertToBytes(message);
        if (msgBytes == null) {
            return;
        }
        send(queue, msgBytes, messageId);

    }

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
            ObjectNode node = JsonUtils.read(msg);
            node.remove("timestamp");
            node.remove("id");
            String className = node.remove("className").asText();
            try {

                return JsonUtils.convertObjectFromJsonNode(node, Class.forName(className));
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
            while (count <= REDIS_MQ_CONFIG.getTimeout()) {
                long reply = jedis.publish(queue, messageId);
                log.info("reply："+reply);
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
