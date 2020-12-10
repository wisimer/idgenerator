package com.xp.id.idgenerator.service;

import cn.hutool.db.nosql.redis.RedisDS;
import com.xp.id.idgenerator.entity.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import redis.clients.jedis.ScanParams;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class IDGeneratorService implements ApplicationListener<ContextRefreshedEvent> {
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
                    ID id = new ID();
                    AtomicLong atomicLong = new AtomicLong();
                    id.atomicLong = atomicLong;
                    initSeqName(name, id);
                }
            }

        }
        return true;
    }

    public void initSeqName(String name, ID id) {
        long maxId = cacheService.atomAddAndGet(name, STEP);
        id.atomicLong.set(maxId - STEP);
        id.maxId = maxId;
        cache.put(name, id);
    }

    /**
     * 保证单纯的自增获取id
     *
     * @param name
     * @return
     */
    public long generate(String name) {
        if (!cache.containsKey(name)) {
            return -1;
        }
        ID id = cache.get(name);
        if (id.atomicLong.longValue() < id.maxId) {
            return id.atomicLong.incrementAndGet();
        } else {
            synchronized (id) {
                initSeqName(name, id);
                return id.atomicLong.incrementAndGet();
            }
        }
    }

    /**
     * @param name
     * @return
     * @deprecated
     */
    public long generator(String name) {

        if (!cache.containsKey(name)) {
            synchronized (cache) {
                if (!cache.containsKey(name)) {
                    ID id = new ID();
                    AtomicLong atomicLong = new AtomicLong();
                    id.atomicLong = atomicLong;
                    initSeqName(name, id);
                }
                return cache.get(name).atomicLong.incrementAndGet();
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

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 游标初始值为0
        String cursor = ScanParams.SCAN_POINTER_START;
        String key = "*";
        Set<String> scanResult = RedisDS.create().getJedis().keys(key);
        for (String s : scanResult) {
            ID id = new ID();
            AtomicLong atomicLong = new AtomicLong();
            id.atomicLong = atomicLong;
            initSeqName(s, id);
        }

    }
}
