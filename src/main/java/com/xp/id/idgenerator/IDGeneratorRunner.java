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
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Component
public class IDGeneratorRunner implements CommandLineRunner {

    @Autowired
    IDGeneratorService idGeneratorService;
    String formmater = ":%d\r\n";
    ArrayList<String> cmdList = new ArrayList<>();

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

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

    public void doWrite(SocketChannel channel, String cmd) throws Throwable {
        //解析命令: *2\r\n$8\r\nSEQUENCE\r\n$3\r\nKEY\r\n
        if (!StringUtils.isEmpty(cmd)) {
            String[] arrs = cmd.replace("\r\n", " ").split(" "); // *2 $8 SEQUENCE $3
            Console.log("arrs : "+arrs.length);
            if (arrs != null && arrs.length > 4 && "*2".equalsIgnoreCase(arrs[0]) && "$8".equalsIgnoreCase(arrs[1]) && "sequence".equalsIgnoreCase(arrs[2])) {
                String key = arrs[4];
                long id = idGeneratorService.generator(key);
                //将缓冲数据写入渠道，返回给客户端
                channel.write(BufferUtil.createUtf8(String.format(formmater, id)));
            } else if (cmd.equals("*2")) { //第一个命令肯定是*2
                cmdList.clear();
                cmdList.add(cmd);
            } else if (cmd.equals("$8") && cmdList.size() == 1) { // 第二个命令是肯定是$8
                cmdList.add(cmd);
            } else if (cmd.equals("sequence") && cmdList.size() == 2) { //第三个命令肯定是sequence
                cmdList.add(cmd);
            } else if (cmd.startsWith("$") && cmdList.size() == 3) { //第四个命令肯定是$开头，跟着一个整数表示seqname的长度
                String cmdRemain = cmd.substring(1);
                if (isInteger(cmdRemain)) {
                    //$后面是整数
                    cmdList.add(cmd);
                } else {
                    //$后面不是整数
                    cmdList.clear();
                    channel.write(BufferUtil.createUtf8("-ERR cmd length should be integer \r\n"));
                }
            } else if (cmdList.size() == 4) {
                if (cmd.length() == Integer.valueOf(cmdList.get(cmdList.size() - 1).substring(1))) {
                    cmdList.add(cmd);
                    long id = idGeneratorService.generator(cmd);
                    channel.write(BufferUtil.createUtf8(String.format(formmater, id)));
                } else {
                    channel.write(BufferUtil.createUtf8("-ERR seqname length error \r\n"));
                }

            } else {
                cmdList.clear();
                channel.write(BufferUtil.createUtf8("-ERR \r\n"));
            }
        } else {
            cmdList.clear();
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
