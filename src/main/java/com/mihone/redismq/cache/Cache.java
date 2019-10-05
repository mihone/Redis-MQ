package com.mihone.redismq.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {
    private static final ConcurrentHashMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    public static void writeToMethodCache(String key, Method value) {
        METHOD_CACHE.put(key, value);
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
