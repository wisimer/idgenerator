package com.xp.id.idgenerator.service;

import cn.hutool.db.nosql.redis.RedisDS;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.util.HashMap;

@Service
public class JedisCacheService implements ICacheService {

    @Override
    public boolean containName(String name) {
        return  RedisDS.create().getJedis().get(name) != null;
    }

    @Override
    public void put(String name, long i) {
        RedisDS.create().getJedis().set(name, String.valueOf(i));
    }

    public long atomAddAndGet(String name, int i) {
        return  RedisDS.create().getJedis().incrBy(name, i);
    }

    @Override
    public long get(String name) {
        return Long.valueOf( RedisDS.create().getJedis().get(name));
    }

}
