package com.mihone.redismq.config;

public class BasicConfig {
    public static final String BASIC_PREFIX = "spring.redis";
    public static final String JEDIS_POOL_PREFIX = "spring.redis.jedis.pool";
    public static final String DEAD_QUEUE_SUFFIX = ":DEAD";
    public static final String BACK_QUEUE_SUFFIX = ":BACK";
}
