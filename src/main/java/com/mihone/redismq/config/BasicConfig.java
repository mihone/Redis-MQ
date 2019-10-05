package com.mihone.redismq.config;

public class BasicConfig {
    public static final String BASIC_PREFIX = "redis";
    public static final String JEDIS_POOL_PREFIX = "redis.jedis.pool";
    public static final String DEAD_QUEUE_SUFFIX = ":DEAD";
    public static final String BACK_QUEUE_SUFFIX = ":BACK";
    public static final long MESSAGE_LOCK_EXPIRE_TIME = 5 * 60 * 1000L;
}
