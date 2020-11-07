package com.xp.id.idgenerator.service;

import com.xp.id.idgenerator.entity.IDEntity;
import com.xp.id.idgenerator.repository.IDRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IDGeneratorService {

    public static final int STEP = 10000;
    Logger logger = LoggerFactory.getLogger(IDGeneratorService.class);
    @Autowired
    LocalCacheService cacheService;
    @Autowired
    IDRepository idRepository;
    AtomicLong atomId = new AtomicLong(0);
    /**
     * 预取ID的最大值
     */
    Long preGenerateMaxId = 0l;

    public synchronized long generator(String name) {
        //如果没有达到预取的最大值，直接使用atomic加1
        if (atomId.compareAndSet(preGenerateMaxId, atomId.incrementAndGet())) {
            IDEntity entity = new IDEntity();
            entity.generatedId = atomId.get();
            entity.name = name;
            entity.time = System.nanoTime();
            idRepository.save(entity);

            return atomId.get();
        }

        //否则再去向缓存预取
        Long curMaxId = 0l;
        if (cacheService.containName(name)) {
            curMaxId = cacheService.get(name);
        }
        preGenerateMaxId = curMaxId + STEP;
        cacheService.put(name, preGenerateMaxId);

        IDEntity entity = new IDEntity();
        entity.generatedId = atomId.get();
        entity.name = name;
        entity.time = System.nanoTime();
        idRepository.save(entity);

        return atomId.get();
    }


}
