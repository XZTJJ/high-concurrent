package com.example.ch03client.tcpClient;


import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

//书上的标准的发送客户端形式
@Component
public class TcpClientSocketPro {
    //日志文件
    private final static Logger LOGGER = LoggerFactory.getLogger(TcpClientSocketPro.class);
    //标准的字符集,用于将文字转成换成字节
    private Charset charset = Charset.forName("utf-8");

    @Autowired
    private Environment environment;


    //客户端的发送方法
    public void tcpClientSend() {
        //源文件
        String sourceFile = environment.getProperty("tcp.socket.sourceFileName", "");
        if (StringUtils.isBlank(sourceFile)) {
            LOGGER.warn("文件为空,直接返回");
            return;
        }
        //获取文件的绝对路径
        int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));
        String serverIp = environment.getProperty("tcp.socket.server.ip", "");
        String serverPort = environment.getProperty("tcp.socket.server.port", "");
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(serverPort)) {
            LOGGER.warn("服务器配置错误,直接返回");
            return;
        }
        //准备开始发送文件
        SocketChannel socketChannel = null;
        FileChannel fileChannel = null;
        //开始准备进行测试
        try {
            //文件前缀
            String fullPrefPath = environment.getProperty("tcp.socket.file.path", "");
            if (StringUtils.isBlank(fullPrefPath))
                fullPrefPath = ResourceUtils.getURL("classpath:").getPath();
            if (StringUtils.isBlank(fullPrefPath)) {
                LOGGER.info("找不到对应文件,直接返回");
                return;
            }
            socketChannel = SocketChannel.open();
            //开始连接，并设置非阻塞
            socketChannel.socket().connect(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)));
            socketChannel.configureBlocking(false);
            //获取文件流
            fileChannel = new FileInputStream(fullPrefPath + File.separator + sourceFile).getChannel();
            long size = fileChannel.size();
            //自选等待
            while (!socketChannel.finishConnect()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
            //开始传输文件
            LOGGER.info("client端连接成功，开始传输文件，文件名: " + sourceFile + "，长度: " + size);
            ByteBuffer encode = charset.encode(sourceFile);
            socketChannel.write(encode);
            //开始创建文件
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            byteBuffer.putLong(size);
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            byteBuffer.clear();

            //开始发送文件
            int length = 0;
            int totalSend = 0;
            while ((length = fileChannel.read(byteBuffer)) > 0) {
                byteBuffer.flip();
                socketChannel.write(byteBuffer);
                byteBuffer.clear();
                totalSend += length;
                LOGGER.info("已发送长度/总长度 : " + totalSend + "/" + size);
            }
            if (length == -1)
                LOGGER.info("文件发送完毕");

        } catch (Exception e) {
            LOGGER.error("发送文件失败", e);
        } finally {
            IOUtils.closeQuietly(fileChannel);
            IOUtils.closeQuietly(socketChannel);

        }
    }


}
