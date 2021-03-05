package com.example.ch03client.tcpClient;

import com.example.ch03client.controller.NioClientController;
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
import java.util.concurrent.TimeUnit;

/**
 *      自己实现的tcpsocket实现类,
 * 将客户端文件上传到服务器上
 */
@Component("mySelfTcpSocket")
public class MySelfTcpClientSocket {
    private final static Logger LOGGER = LoggerFactory.getLogger(NioClientController.class);

    //用户获取环境变量
    @Autowired
    private Environment environment;

    /**
     *      在实际使用的网络交互中，基本上没有直接使用Socket套接字的
     * 一般而言，都是直接http方式，socket是作为更加底层的一种方式进行
     * 开发的
     *      主要实现的目标是是将客户端的文件上传到服务器端
     */
    public void clientTcpSocket() {
        Long startTime = System.currentTimeMillis();
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

        //读取文件
        String fullPrefPath = null;
        FileInputStream fileInputStream = null;
        FileChannel fileInChannel = null;
        //客户端socket通道
        SocketChannel clientSocketChannel = null;
        ByteBuffer byteBuffer = null;
        //客户端socket通道
        try {
            fullPrefPath = environment.getProperty("tcp.socket.file.path", "");
            if (StringUtils.isBlank(fullPrefPath))
                fullPrefPath = ResourceUtils.getURL("classpath:").getPath();
            if (StringUtils.isBlank(fullPrefPath)) {
                LOGGER.info("找不到对应文件,直接返回");
                return;
            }
            //先进行socket的连接
            clientSocketChannel = SocketChannel.open();
            //客户端需要先设置连接，然后在设置非阻塞
            clientSocketChannel.socket().connect(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)));
            clientSocketChannel.configureBlocking(false);
            //读取文件，并且获取通道
            fileInputStream = new FileInputStream(fullPrefPath + File.separator + sourceFile);
            fileInChannel = fileInputStream.getChannel();
            //缓存区
            byteBuffer = ByteBuffer.allocate(bufferSize);
            //获取总的字节长度
            long size = fileInChannel.size();
            if (size == 0) {
                LOGGER.warn("等待上传文件内容为空,直接返回");
                return;
            }
            //等待时间
            long waitForConnectTime = Long.valueOf(environment.getProperty("tcp.socket.waitForConnect.time", "10000"));
            long waitTime = System.currentTimeMillis();
            //咨询等待进行连接,超时设置
            while (!clientSocketChannel.finishConnect()) {
                try {
                    if (System.currentTimeMillis() - waitTime > waitForConnectTime) {
                        LOGGER.error("连接超时");
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (Exception e1) {
                    LOGGER.error("等待连接，休眠失败");
                }
            }
            LOGGER.info("开始准备传输文件,文件的名称为:" + sourceFile + ",文件总长度为:" + size);
            //首先上传文件名，大小
            byteBuffer.put(StringUtils.getBytes(sourceFile, "utf-8"));
            byteBuffer.flip();
            clientSocketChannel.write(byteBuffer);
            byteBuffer.clear();
            byteBuffer.put(StringUtils.getBytes(size + "", "utf-8"));
            byteBuffer.flip();
            clientSocketChannel.write(byteBuffer);
            byteBuffer.clear();
            //开始准备开始上传数据
            int postion = 0;
            int mark = 0;
            while ((postion = fileInChannel.read(byteBuffer)) > 0) {
                mark += postion;
                //模式转换
                byteBuffer.flip();
                clientSocketChannel.write(byteBuffer);
                byteBuffer.clear();
                LOGGER.info("已上传长度/总长度 : " + mark + "/" + size);
            }
            LOGGER.info("上传所有文件耗费的时间为:" + (System.currentTimeMillis() - startTime));
        } catch (Exception e) {
            LOGGER.error("文件上传失败", e);
        } finally {
            //关闭对应的流
            IOUtils.closeQuietly(clientSocketChannel);
            IOUtils.closeQuietly(fileInChannel);
            IOUtils.closeQuietly(fileInputStream);
        }
    }

}
