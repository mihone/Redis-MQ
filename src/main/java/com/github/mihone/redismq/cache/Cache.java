package com.github.mihone.redismq.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class Cache {
    private static final ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Future, String> QUEUE_LISTENER_CACHE = new ConcurrentHashMap<>();

    public static void writeToMethodCache(String key, Method value) {
        METHOD_CACHE.put(key, value);
    }

    public static String getFromQueueListenerCache(Future key) {
        return QUEUE_LISTENER_CACHE.get(key);
    }
    public static void writeToQueueListenerCache(Future key, String value) {
        QUEUE_LISTENER_CACHE.put(key, value);
    }
    public static void removeFromQueueListenerCache(Future key) {
        QUEUE_LISTENER_CACHE.remove(key);
    }

    public static Method getFromMethodCache(String key) {
        return METHOD_CACHE.get(key);
    }

    public static List<String> getCurrentCacheKeysFromMethodCache() {
        ConcurrentHashMap.KeySetView<String, Method> keySetView = METHOD_CACHE.keySet();
        ArrayList<String> list = new ArrayList<>();
        list.addAll(keySetView);
        return list;
    }
}
