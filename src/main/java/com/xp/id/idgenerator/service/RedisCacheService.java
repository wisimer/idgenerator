//package com.xp.id.idgenerator.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.support.atomic.RedisAtomicLong;
//import org.springframework.stereotype.Service;
//
//@Service
//public class RedisCacheService implements ICacheService {
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Override
//    public boolean containName(String name) {
//        return redisTemplate.hasKey(name);
//    }
//
//    @Override
//    public void put(String name, long i) {
//        redisTemplate.opsForValue().set(name, i);
//    }
//
//    public long atomAddAndGet(String name, int i) {
//
//        return redisTemplate.opsForValue().increment(name, i);
//    }
//
//    @Override
//    public long get(String name) {
//        return (long) redisTemplate.opsForValue().get(name);
//    }
//}
