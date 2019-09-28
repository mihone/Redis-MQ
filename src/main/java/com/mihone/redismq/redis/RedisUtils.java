package com.mihone.redismq.redis;

import com.mihone.redismq.config.RedisPoolConfig;
import com.mihone.redismq.yaml.YmlUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class RedisUtils {
    private RedisUtils() {
    }


    public static void returnJedis(Jedis jedis){
        if (jedis != null) {
            jedis.close();
        }
    }
    public static Jedis getJedis(){
        return RedisPool.pool.getResource();
    }

    private static final class RedisPool {
        private static final String BASIC_PREFIX = "spring.redis";
        private static final String JEDIS_POOL_PREFIX = "spring.redis.jedis.pool";
        private static final RedisPoolConfig CONFIG = new RedisPoolConfig();

        static {
            Object url = YmlUtils.getValue(BASIC_PREFIX + ".url");
            if (url != null) {
                CONFIG.setUrl(url.toString());
            }
            Object port = YmlUtils.getValue(BASIC_PREFIX + ".port");
            if (port != null) {
                CONFIG.setPort(Integer.parseInt(port.toString()));
            }
            Object password = YmlUtils.getValue(BASIC_PREFIX + ".password");
            if (password != null) {
                CONFIG.setPassword(password.toString());
            }
            Object timeout = YmlUtils.getValue(BASIC_PREFIX + ".timeout");
            if (timeout != null) {
                CONFIG.setTimeout(Integer.parseInt(timeout.toString()));
            }
            Object database = YmlUtils.getValue(BASIC_PREFIX + ".database");
            if (database != null) {
                CONFIG.setDatabase(Integer.parseInt(database.toString()));
            }
            Object maxActive = YmlUtils.getValue(JEDIS_POOL_PREFIX + ".max-active");
            if (maxActive != null) {
                CONFIG.setMaxTotal(Integer.parseInt(maxActive.toString()));
            }
            Object maxIdle = YmlUtils.getValue(JEDIS_POOL_PREFIX + ".max-idle");
            if (maxIdle != null) {
                CONFIG.setMaxTotal(Integer.parseInt(maxIdle.toString()));
            }
            Object minIdle = YmlUtils.getValue(JEDIS_POOL_PREFIX + ".min-idle");
            if (minIdle != null) {
                CONFIG.setMaxTotal(Integer.parseInt(minIdle.toString()));
            }
            Object maxWait = YmlUtils.getValue(JEDIS_POOL_PREFIX + ".max-wait");
            if (maxWait != null) {
                CONFIG.setMaxTotal(Integer.parseInt(maxWait.toString()));
            }
        }

        private static final JedisPool pool = new JedisPool(CONFIG,CONFIG.getUrl(),CONFIG.getPort(), CONFIG.getTimeout(),CONFIG.getPassword(),CONFIG.getDatabase());

    }
}
