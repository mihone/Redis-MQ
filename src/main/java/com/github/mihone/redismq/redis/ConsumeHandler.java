package com.github.mihone.redismq.redis;

import com.github.mihone.redismq.log.Log;
import com.github.mihone.redismq.reflect.MethodInvocationHandler;
import com.github.mihone.redismq.cache.Cache;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.Method;

public class ConsumeHandler extends JedisPubSub {
    private static final Log log = Log.getLogger(ConsumeHandler.class);
    @Override
    public void onMessage(String channel, String message) {
        log.info("监听到...开始执行"+channel+",message:"+message);
        Method method = Cache.getFromMethodCache(channel);
        try {
            MethodInvocationHandler.handler(method,channel,message);
        } catch (Exception e) {
           log.error("method invoke failed.Cause:{}",e);
        }
    }
}

