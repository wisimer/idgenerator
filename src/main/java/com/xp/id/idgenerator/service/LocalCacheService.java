package com.xp.id.idgenerator.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LocalCacheService implements ICacheService {
    public ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    @Override
    public boolean containName(String name) {
        if (cache.containsKey(name)) {
            return true;
        }
        return false;
    }

    @Override
    public void put(String name, long i) {
        cache.put(name, i);
    }

    @Override
    public long get(String name) {
        return cache.get(name);
    }
}
