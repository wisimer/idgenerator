package com.xp.id.idgenerator.service;

import org.springframework.stereotype.Service;

@Service
public class RedisCacheService implements ICacheService {


    @Override
    public boolean containName(String name) {
        return false;
    }

    @Override
    public void put(String name, long i) {

    }

    @Override
    public long get(String name) {
        return 0;
    }
}
