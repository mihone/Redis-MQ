package com.github.mihone.redismq.config;

import com.github.mihone.redismq.yaml.YmlUtils;

public class RedisMqConfig {
    private static final int DEFAULT_TIMEOUT = 10;
    private static final long DEFAULT_DELAY_CHECK_INTERVAL = 1000;
    private static final long DEFAULT_BACK_CHECK_INTERVAL = 30000;
    private static final long DEFAULT_DEAD_CHECK_INTERVAL = 30000;
    private static final long DEFAULT_HEART_BEAT_INTERVAL = 60000;
    private static int timeout = DEFAULT_TIMEOUT;
    private static long delayCheckInterval = DEFAULT_DELAY_CHECK_INTERVAL;
    private static long backCheckInterval = DEFAULT_BACK_CHECK_INTERVAL;
    private static long deadCheckInterval = DEFAULT_DEAD_CHECK_INTERVAL;
    private static long heartBeatInterval = DEFAULT_HEART_BEAT_INTERVAL;
    private static String[] delayQueues = {};
    private static String[] queues = {};

    public static int getTimeout() {
        return timeout;
    }

    public static int getDefaultTimeout() {
        return DEFAULT_TIMEOUT;
    }

    public static long getDelayCheckInterval() {
        return delayCheckInterval;
    }

    public static long getDefaultDelayCheckInterval() {
        return DEFAULT_DELAY_CHECK_INTERVAL;
    }

    public static long getBackCheckInterval() {
        return backCheckInterval;
    }

    public static long getDefaultBackCheckInterval() {
        return DEFAULT_BACK_CHECK_INTERVAL;
    }

    public static long getDeadCheckInterval() {
        return deadCheckInterval;
    }

    public static long getDefaultDeadCheckInterval() {
        return DEFAULT_DEAD_CHECK_INTERVAL;
    }

    public static long getHeartBeatInterval() {
        return heartBeatInterval;
    }

    public static long getDefaultHeartBeatInterval() {
        return DEFAULT_HEART_BEAT_INTERVAL;
    }

    public static String[] getDelayQueues() {
        return delayQueues;
    }

    public static String[] getQueues() {
        return queues;
    }

    static {
        Object timeoutParam = YmlUtils.getValue("redis.mq.timeout");
        if (timeoutParam != null) {
            timeout = Integer.parseInt(timeoutParam.toString());
        }
        String dQueues = (String) YmlUtils.getValue("redis.mq.delayQueues");
        if (dQueues != null) {
            if (dQueues.indexOf(",") == 0) {
                dQueues = dQueues.substring(1, dQueues.length());
            }
            delayQueues = dQueues.split(",");
        }
        String allQueues = (String) YmlUtils.getValue("redis.mq.queues");
        if (allQueues != null) {
            if (allQueues.indexOf(",") == 0) {
                allQueues = allQueues.substring(1, allQueues.length());
            }
            queues = allQueues.split(",");
        }
        Object delayInterval = YmlUtils.getValue("redis.mq.delayCheckInterval");
        if (delayInterval != null) {
            delayCheckInterval = Integer.parseInt(delayInterval.toString());
        }
        Object backInterval = YmlUtils.getValue("redis.mq.backCheckInterval");
        if (backInterval != null) {
            backCheckInterval = Integer.parseInt(backInterval.toString());
        }
        Object deadInterval = YmlUtils.getValue("redis.mq.deadCheckInterval");
        if (deadInterval != null) {
            deadCheckInterval = Integer.parseInt(deadInterval.toString());
        }
        Object heartInterval = YmlUtils.getValue("redis.mq.heartBeatInterval");
        if (heartInterval != null) {
            heartBeatInterval = Integer.parseInt(heartInterval.toString());
        }
    }
}
