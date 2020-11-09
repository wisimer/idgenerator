package com.xp.id.idgenerator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

@SpringBootTest
class IDEntityGeneratorApplicationTests {

    @Test
    void contextLoads() {
        long start = System.currentTimeMillis();
        int j = 0;
        for (int i = 0; i < 100000; i++) {
            j++;
        }
        long dur = System.currentTimeMillis() - start;
        System.out.println("间隔：" + dur + "");
    }


}
