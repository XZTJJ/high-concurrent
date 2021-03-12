package com.example.ch04.MultiThread.client;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *  书上的给的一个实例 ：多线程的Reactor模式下面的客户端
 */
@Component
public class MultiThreadClientPro {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadClientPro.class);
    //环境变量

    public void clientTest() {
        SocketChannel socketChannel = null;
        Selector selector = null;
        try {
            //选择器
            selector = Selector.open();
            ResourceBundle resource = ResourceBundle.getBundle("application");
            //ip和端口设置
            String serverIP = resource.getString("reactor.socket.communcation.ip");
            String serverPort = resource.getString("reactor.socket.communcation.port");
            //创建连接
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
            socketChannel.configureBlocking(false);
            //注册的key
            SelectionKey registerKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            //开始进行连接
            while (!socketChannel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(100);
            }
            LOG.info("客户端连接成功");
            //选择键
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey readyKey = iterator.next();
                    //判断消息的类型
                    if (readyKey.isReadable()) {
                        //读取数据
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        int length = 0;
                        while ((length = socketChannel.read(byteBuffer)) > 0) {
                            byteBuffer.flip();
                            LOG.info("客户端收消息 : " + new String(byteBuffer.array(), 0, length));
                            byteBuffer.clear();
                        }
                    } else if (readyKey.isWritable()) {
                        //发消息
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        buffer.put((LocalDateTime.now().toString() + " ~~~~~ ").getBytes());
                        buffer.flip();
                        // 操作三：通过DatagramChannel数据报通道发送数据
                        socketChannel.write(buffer);
                        LOG.info("客户端发消息 : " + new String(buffer.array(), 0, buffer.limit()));
                        buffer.clear();
                        TimeUnit.MILLISECONDS.sleep(2000);
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

}
