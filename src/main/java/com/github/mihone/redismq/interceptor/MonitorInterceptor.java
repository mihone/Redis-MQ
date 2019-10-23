package com.github.mihone.redismq.interceptor;

import redis.clients.jedis.Jedis;

/**
 * <p>A monitor interceptor which can decide if monitor threads work or not .
 * <p>monitor threads are executed by scheduled so this will be executed every time.
 * <p>The purpose of this interceptor is to adapt the requirement that start monitor in the consumers' service.
 * However , it is not recommended because it might increase the burden of consumers.
 *
 * @author mihone
 * @since 2019/10/14
 */
public interface MonitorInterceptor {
    default boolean delay(Jedis jedis) {
        return true;
    }

    default boolean back(Jedis jedis) {
        return true;
    }

    default boolean dead(Jedis jedis) {
        return true;
    }

}
