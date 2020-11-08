package com.xp.id.idgenerator.service;

import com.xp.id.idgenerator.entity.IDEntity;
import com.xp.id.idgenerator.repository.IDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IDGeneratorService {
    public static final int STEP = 1000;
    public static final boolean SALT = false;
    @Autowired
    RedisCacheService cacheService;
    @Autowired
    IDRepository idRepository;

    private AtomicLong atomId = new AtomicLong(0);
    /**
     * 预取ID的最大值
     */
    private Long preGenerateMaxId = 1l;
    private Logger logger = LoggerFactory.getLogger(IDGeneratorService.class);

    public synchronized long generator(String name) {
        //如果没有达到预取的最大值，直接使用atomic加1
        if (!atomId.compareAndSet(preGenerateMaxId, atomId.incrementAndGet())) {
            return atomId.get();
        }
        //否则再去向缓存预取
        preGenerateMaxId = cacheService.atomAddAndGet(name, STEP);
        return atomId.incrementAndGet();
    }


}
