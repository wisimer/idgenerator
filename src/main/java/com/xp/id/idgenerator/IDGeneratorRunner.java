package com.xp.id.idgenerator;

import cn.hutool.core.io.BufferUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import cn.hutool.socket.nio.NioServer;
import com.xp.id.idgenerator.service.IDGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Set;

@Component
public class IDGeneratorRunner implements CommandLineRunner {
    //接受数据缓冲区
    private static ByteBuffer sBuffer = ByteBuffer.allocate(1024);
    //发送数据缓冲区
    private static ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    //选择器（叫监听器更准确些吧应该）
    private static Selector selector;
    @Autowired
    IDGeneratorService idGeneratorService;
    //解码buffer
    private Charset cs = Charset.forName("UTF-8");

    @Override
    public void run(String... args) throws Exception {
        NioServer server = new NioServer(8081);
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
                    String key = StrUtil.utf8Str(bytes);
                    Console.log("[{}]: {}", sc.getRemoteAddress(), key);
                    doWrite(sc, key);
                } else if (readBytes < 0) {
                    IoUtil.close(sc);
                }
            } catch (IOException e) {
                throw new IORuntimeException(e);
            }
        });
        server.listen();
    }

    public void doWrite(SocketChannel channel, String key) throws IOException {
        long id = idGeneratorService.generator(key);
        //将缓冲数据写入渠道，返回给客户端
        channel.write(BufferUtil.createUtf8(id + ""));
    }

}
