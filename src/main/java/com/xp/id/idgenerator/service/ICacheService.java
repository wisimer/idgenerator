package com.xp.id.idgenerator.service;

import org.springframework.stereotype.Service;

@Service
public interface ICacheService {

    boolean containName(String name);

    void put(String name, long i);

    long get(String name);
}
