package com.example.ch03server.udpServer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


//自己手写的 , udp服务器
@Component
public class MySelfUDPServer {
    //日志
    private final static Logger LOGGER = LoggerFactory.getLogger(MySelfUDPServer.class);
    //字符集
    private Charset charset = Charset.forName("utf-8");
    @Autowired
    private Environment environment;

    //书上的
    public void myUDPServer() {
        //设置服务器相关信息，服务器端只需要监听端口就行
        String serverIp = environment.getProperty("udp.socket.server.ip", "");
        String serverPort = environment.getProperty("udp.socket.server.port", "");
        //设置服务
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(serverPort)) {
            LOGGER.warn("服务器设置错误");
            return;
        }
        //选择器
        Selector selector = null;
        //服务端数据流
        DatagramChannel datagramChannel = null;
        //就绪的IO事件
        SelectionKey readyKey = null;
        try {
            //注册服务器对应的事件
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            datagramChannel.bind(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)));
            //选择器,并且注销到对应的选择器上
            selector = Selector.open();
            datagramChannel.register(selector, SelectionKey.OP_READ);
            //开始选择，选择器开始选择
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                //循环迭代
                while (iterator.hasNext()) {
                    readyKey = iterator.next();
                    //接受事件处理函数
                    if (readyKey.isReadable()) {
                        int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));
                        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
                        int length = 0;
                        SocketAddress receiveAddr = datagramChannel.receive(byteBuffer);
                        byteBuffer.flip();
                        String content = charset.decode(byteBuffer).toString();
                        LOGGER.info("接受客户端为 : " + receiveAddr + " ，接受内容为 : " + content);
                    } else if (readyKey.isWritable()) {
                        LOGGER.info("没有可写事件~~~~~~");
                    }
                    //移除对应的key
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            LOGGER.error("服务器端接受文件失败", e);
            if (readyKey != null)
                readyKey.cancel();
        } finally {
            IOUtils.closeQuietly(datagramChannel);
            IOUtils.closeQuietly(selector);
        }
    }

}
