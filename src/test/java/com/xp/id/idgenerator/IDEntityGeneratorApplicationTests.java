package com.xp.id.idgenerator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IDEntityGeneratorApplicationTests {

    @Test
    void contextLoads() {
        long start = System.currentTimeMillis();
        int j = 0;
        for (int i = 0; i < 100000; i++) {
            j++;
        }
        long dur = System.currentTimeMillis()-start;
        System.out.println("间隔："+dur+"");
    }

}
