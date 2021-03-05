package com.example.ch03client.udpClient;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

//自己手写的 UDP的上传的功能客户端
@Component
public class MySelfUDPClient {
    //日志文件
    private final static Logger LOGGER = LoggerFactory.getLogger(MySelfUDPClient.class);
    //标准的字符集,用于将文字转成换成字节
    private Charset charset = Charset.forName("utf-8");

    @Autowired
    private Environment environment;

    public void mySelfUDPClient() {
        String serverIp = environment.getProperty("udp.socket.server.ip", "");
        String serverPort = environment.getProperty("udp.socket.server.port", "");
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(serverPort)) {
            LOGGER.warn("服务器配置错误,直接返回");
            return;
        }

        DatagramChannel datagramChannel = null;
        FileChannel fileChannel = null;
        try {
            //准备数据报文,udp是在发送文件的时候才需要绑定ip的,还不用担心等待连接的问题
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            //对应IP信息
            InetSocketAddress inetSocketAddress = new InetSocketAddress(serverIp, Integer.valueOf(serverPort));
            //循环发送一些数据就好
            for (int i = 0; i < 5; i++) {
                ByteBuffer encode = charset.encode("当前的时间为:" + LocalDateTime.now().toString() + " , 这是第" + i + "个消息");
                datagramChannel.send(encode, inetSocketAddress);
            }
            LOGGER.info("消息发送完毕");
        } catch (Exception e) {
            LOGGER.error("发送文件失败", e);
        } finally {
            IOUtils.closeQuietly(fileChannel);
            IOUtils.closeQuietly(datagramChannel);
        }
    }
}
