package com.example.ch04.SingleThread.client;


import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class MySelfClient {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MySelfClient.class);
    //环境变量
    @Autowired
    private Environment environment;

    public void clientTest() {
        SocketChannel socketChannel = null;
        Selector selector = null;
        try {
            int isContinue = 0;
            //选择器
            selector = Selector.open();
            //ip和端口设置
            String serverIP = environment.getProperty("reactor.socket.communcation.ip");
            String serverPort = environment.getProperty("reactor.socket.communcation.port");
            int bufferSize = Integer.valueOf(environment.getProperty("reactor.socket.readSize", "1024"));
            //限制通信次数
            int limitCount = Integer.valueOf(environment.getProperty("reactor.socket.communcation.count", "2"));
            //创建连接
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            //开始进行连接
            while (!socketChannel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            LOG.info("客户端连接成功");
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            //选择键
            while (!isNeedClose(isContinue, limitCount) && selector.select() > 0) {
                byteBuffer.clear();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey readyKey = iterator.next();
                    SocketChannel readyChannel = (SocketChannel) readyKey.channel();
                    byteBuffer.clear();
                    //判断消息的类型
                    if (readyKey.isReadable()) {
                        readyChannel.read(byteBuffer);
                        LOG.info("客户端收消息 : " + new String(byteBuffer.array(), 0, byteBuffer.position()));
                        isContinue++;
                    } else if (readyKey.isWritable()) {
                        //进行写操作
                        String content = LocalDateTime.now().toString() + "发送了一个消息~";
                        LOG.info("客户端发消息 : " + content);
                        byteBuffer.put(content.getBytes());
                        byteBuffer.flip();
                        readyChannel.write(byteBuffer);
                        TimeUnit.MILLISECONDS.sleep(500);
                    }
                    //到达次数取消注册
                    if (isNeedClose(isContinue, limitCount)) {
                        readyKey.cancel();
                    }
                }
                //选择集的清除
                selectionKeys.clear();
            }

        } catch (Exception e) {
            LOG.error("连接异常", e);
        } finally {
            LOG.info("客户端准备关闭连接");
            IOUtils.closeQuietly(socketChannel);
            IOUtils.closeQuietly(selector);
        }
    }

    private boolean isNeedClose(int count, int limitCount) {

        return count > limitCount ? true : false;
    }


}
