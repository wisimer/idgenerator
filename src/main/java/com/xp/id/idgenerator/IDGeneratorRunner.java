package com.xp.id.idgenerator;

import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.socket.nio.NioClient;
import cn.hutool.socket.nio.NioServer;
import com.xp.id.idgenerator.service.IDGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Scanner;
import java.util.Set;

@Component
public class IDGeneratorRunner implements CommandLineRunner {

    @Autowired
    IDGeneratorService idGeneratorService;

    @Override
    public void run(String... args) throws Exception {
        NioServer server = new NioServer(8073);
        server.setChannelHandler((sc) -> {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            try {
                //从channel读数据到缓冲区
                int readBytes = sc.read(readBuffer);
                if (readBytes > 0) {
                    //Flips this buffer.  The limit is set to the current position and then
                    // the position is set to zero，就是表示要从起始位置开始读取数据
                    readBuffer.flip();
                    //eturns the number of elements between the current position and the  limit.
                    // 要读取的字节长度
                    byte[] bytes = new byte[readBuffer.remaining()];
                    //将缓冲区的数据读到bytes数组
                    readBuffer.get(bytes);
                    String cmd = StrUtil.utf8Str(bytes);
                    Console.log("[{}]: {}", sc.getRemoteAddress(), cmd);

                    if ("exit".equalsIgnoreCase(cmd)) {
                        IoUtil.close(sc);
                    } else {
                        doWrite(sc, cmd);
                    }

                } else if (readBytes < 0) {
                    IoUtil.close(sc);
                }
            } catch (IOException e) {
                throw new IORuntimeException(e);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        server.listen();
    }

    String formmater = ":%d\r\n";
    public void doWrite(SocketChannel channel, String cmd) throws Throwable {
        //解析命令: *2\r\n$8\r\nSEQUENCE\r\n$3\r\nKEY\r\n
        String[] arrs = cmd.replace("\\r\\n", " ").split(" "); // *2 $8 SEQUENCE $3
        if ("*2".equalsIgnoreCase(arrs[0]) && "$8".equalsIgnoreCase(arrs[1]) && "sequence".equalsIgnoreCase(arrs[2]) && arrs.length > 4) {
            String key = arrs[4];
            long id = idGeneratorService.generator(key);
            //将缓冲数据写入渠道，返回给客户端
            channel.write(BufferUtil.createUtf8(String.format(formmater,id)));
        } else {
            channel.write(BufferUtil.createUtf8("-ERR no id \r\n"));
        }

    }


    public void connectRedis(SocketChannel channel, String cmd) throws Throwable {
        NioClient client = new NioClient("127.0.0.1", 6379);
        client.setChannelHandler((sc) -> {
            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            //从channel读数据到缓冲区
            int readBytes = sc.read(readBuffer);
            if (readBytes > 0) {
                //Flips this buffer.  The limit is set to the current position and then
                // the position is set to zero，就是表示要从起始位置开始读取数据
                readBuffer.flip();
                //returns the number of elements between the current position and the  limit.
                // 要读取的字节长度
                byte[] bytes = new byte[readBuffer.remaining()];
                //将缓冲区的数据读到bytes数组
                readBuffer.get(bytes);
                String body = StrUtil.utf8Str(bytes);
                Console.log("[{}]: {}", sc.getRemoteAddress(), body);
            } else if (readBytes < 0) {
                sc.close();
            }
        });
        client.listen();
        client.write(BufferUtil.createUtf8(cmd));
    }

}
