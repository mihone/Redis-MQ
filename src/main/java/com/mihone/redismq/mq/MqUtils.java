package com.mihone.redismq.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.mihone.redismq.config.BasicConfig;
import com.mihone.redismq.config.RedisMqConfig;
import com.mihone.redismq.json.JsonUtils;
import com.mihone.redismq.redis.RedisUtils;
import redis.clients.jedis.Jedis;

public class MqUtils {
    private static final RedisMqConfig REDIS_MQ_CONFIG = new RedisMqConfig();

    private MqUtils() {
    }

    public static void sendMessage(final String queue, final String channelName, final Object data) {
        long messageId = System.currentTimeMillis();
        JsonNode message = JsonUtils.addAttribute(data, "timestamp", messageId);
        final byte[] msgBytes = JsonUtils.convertToBytes(message);
        Jedis jedis = RedisUtils.getJedis();
        Long result = jedis.lpush(queue.getBytes(), msgBytes);
        int count = 0;
        while (count <= REDIS_MQ_CONFIG.getTimeout()) {
            long reply = jedis.publish(channelName, messageId + "");
            try {
                if (reply == 0) {
                    count++;
                    Thread.sleep(1000);
                } else {
                    break;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException("sleep state is interrupted");
            }
        }
        jedis.lrem(queue.getBytes(), 0, msgBytes);
        jedis.lpush((queue + BasicConfig.DEAD_QUEUE_SUFFIX).getBytes(), msgBytes);
        jedis.close();
    }
}
