package com.mihone.redismq.config;

import com.mihone.redismq.redis.RedisUtils;
import com.mihone.redismq.yaml.YmlUtils;

public class RedisMqConfig {
    public static final int DEFAULT_TIMEOUT = 10;
    private int timeout = DEFAULT_TIMEOUT;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    static {
        Object timeout = YmlUtils.getValue("spring.redis.mq.timeout");
        if(timeout!=null){
            timeout = Integer.parseInt(timeout.toString());
        }
    }
}
