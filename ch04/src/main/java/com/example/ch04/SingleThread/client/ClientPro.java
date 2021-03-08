package com.example.ch04.SingleThread.client;

import org.omg.CORBA.TIMEOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import sun.misc.IOUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.spi.LocaleNameProvider;

//书上给的客户端实例代码
@Component
public class ClientPro {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(ClientPro.class);
    //环境变量
    @Autowired
    private Environment environment;
    //通道和选择器
    private SocketChannel socketChannel;
    private Selector selector;


    //客户端处理方法
    public void clientPro() {
        try {
            //ip和端口设置
            String serverIP = environment.getProperty("reactor.socket.communcation.ip");
            String serverPort = environment.getProperty("reactor.socket.communcation.port");
            int bufferSize = Integer.valueOf(environment.getProperty("reactor.socket.readSize", "1024"));
            //打开连接，设置非阻塞，连接成功后，注册
            socketChannel = SocketChannel.open(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
            socketChannel.configureBlocking(false);
            while (!socketChannel.finishConnect())
                TimeUnit.MILLISECONDS.sleep(800);
            LOG.info("客户端连接成功");

            //选择器和注册
            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            //循环开始
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                //循环处理
                while (iterator.hasNext()) {
                    SelectionKey readyKey = iterator.next();
                    //如果是可读的事件
                    if (readyKey.isReadable()) {
                        // 若选择键的IO事件是“可读”事件,读取数据
                        SocketChannel socketChannel = (SocketChannel) readyKey.channel();

                        //读取数据
                        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
                        int length = 0;
                        while ((length = socketChannel.read(byteBuffer)) > 0) {
                            byteBuffer.flip();
                            LOG.info("客户端收消息 : " + new String(byteBuffer.array(), 0, length));
                            byteBuffer.clear();
                        }
                    } else if (readyKey.isWritable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);

                        String content = LocalDateTime.now().toString() + "发送了一个消息~";
                        LOG.info("客户端发消息 : " + content);
                        buffer.put(content.getBytes());
                        buffer.flip();
                        socketChannel.write(buffer);
                        TimeUnit.MILLISECONDS.sleep(2000);
                    }
                }
                //
                selectionKeys.clear();
            }
        } catch (Exception e) {
            LOG.error("客户端连接出现错误", e);
        }

    }
}
