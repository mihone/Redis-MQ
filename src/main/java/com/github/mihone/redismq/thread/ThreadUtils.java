package com.github.mihone.redismq.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadUtils {
    private static final ExecutorService CACHE_THREAD_POOL = Executors.newCachedThreadPool();

    public static void submit(Runnable runnable){
        CACHE_THREAD_POOL.submit(runnable);
    }

    private ThreadUtils() {
    }
}
