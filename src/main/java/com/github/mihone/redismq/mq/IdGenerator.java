package com.github.mihone.redismq.mq;

public class IdGenerator {

    private static long lastTime = -1L;

    private static long sequence = 0L;
    private static final long SEQUENCE_LENGTH = 22;
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_LENGTH);

    /**
     * Generate Message id
     * @return id
     * @author mihone
     * @since 2019/10/6
     */
    public static synchronized long generateId() {
        long nowTime = getNowTime();
        if (nowTime < lastTime) {
            throw new RuntimeException();
        }
        if (nowTime == lastTime) {
            sequence += 1;
        } else {
            sequence = 0;
        }
        lastTime = nowTime;
        return nowTime << SEQUENCE_LENGTH | sequence;
    }

    private static long getNowTime() {
        return System.currentTimeMillis();
    }
}
