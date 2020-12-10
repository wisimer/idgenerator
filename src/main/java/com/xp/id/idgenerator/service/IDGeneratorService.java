package com.xp.id.idgenerator.service;

import com.xp.id.idgenerator.entity.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IDGeneratorService {
    public static final int STEP = 100000;
    public static final boolean SALT = false;
    @Autowired
    JedisCacheService cacheService;

    /**
     * 预取ID的最大值
     */
    private Logger logger = LoggerFactory.getLogger(IDGeneratorService.class);

    private ConcurrentHashMap<String, ID> cache = new ConcurrentHashMap<>();

    /**
     * 提前注册，避免生成id时的无效判断，保证id单纯做一个自增获取的操作。
     *
     * @param name
     * @return
     */
    public boolean register(String name) {
        if (!cache.containsKey(name)) {
            synchronized (cache) {
                if (!cache.containsKey(name)) {
                    long maxId = cacheService.atomAddAndGet(name, STEP);
                    AtomicLong atomicLong = new AtomicLong(maxId - STEP);
                    ID id = new ID();
                    id.atomicLong = atomicLong;
                    id.maxId = maxId;
                    cache.put(name, id);
                }
            }

        }
        return true;
    }

    /**
     * 保证单纯的自增获取id
     * @param name
     * @return
     */
    public long generator(String name) {
        ID id = cache.get(name);
        if (id.atomicLong.longValue() < id.maxId) {
            return id.atomicLong.incrementAndGet();
        } else {
            synchronized (id) {
                long maxid = cacheService.atomAddAndGet(name, STEP);
                id.maxId = maxid;
                id.atomicLong.set(maxid - STEP + 1);
                return id.atomicLong.longValue();
            }
        }
    }

    /**
     * @deprecated
     * @param name
     * @return
     */
    public long generate(String name) {

        if (!cache.containsKey(name)) {
            synchronized (cache) {
                if (!cache.containsKey(name)) {
                    long maxId = cacheService.atomAddAndGet(name, STEP);
                    AtomicLong atomicLong = new AtomicLong(maxId - STEP + 1);
                    ID id = new ID();
                    id.atomicLong = atomicLong;
                    id.maxId = maxId;
                    cache.put(name, id);
                    return cache.get(name).atomicLong.longValue();
                } else {
                    return cache.get(name).atomicLong.incrementAndGet();
                }
            }
        } else {
            ID id = cache.get(name);
            if (id.atomicLong.longValue() < id.maxId) {
                return id.atomicLong.incrementAndGet();
            } else {
                synchronized (id) {
                    long maxid = cacheService.atomAddAndGet(name, STEP);
                    id.maxId = maxid;
                    id.atomicLong.set(maxid - STEP + 1);
                    return id.atomicLong.longValue();
                }
            }

        }
    }

}
