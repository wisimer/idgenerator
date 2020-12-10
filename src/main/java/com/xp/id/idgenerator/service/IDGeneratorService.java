package com.xp.id.idgenerator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

@Service
public class IDGeneratorService {
    public static final int STEP = 100000;
    public static final boolean SALT = false;
    @Autowired
    JedisCacheService cacheService;

    /**
     * 预取ID的最大值
     */
    private ConcurrentHashMap<String, Long> preGenerateMaxIds = new ConcurrentHashMap<>();
    private Logger logger = LoggerFactory.getLogger(IDGeneratorService.class);

    private ConcurrentHashMap<String, Long> cache = new ConcurrentHashMap<>();

    public synchronized long generator(String name) {

        if (cache.containsKey(name) && cache.get(name) < preGenerateMaxIds.get(name)) {
            //如果没有达到预取的最大值，直接使用atomic加1
            cache.put(name, cache.get(name) + 1);
            return cache.get(name);
        } else {
            long maxId = cacheService.atomAddAndGet(name, STEP);
            preGenerateMaxIds.put(name, maxId);
            cache.put(name, maxId-STEP+1);
            return cache.get(name);
        }

    }


}
