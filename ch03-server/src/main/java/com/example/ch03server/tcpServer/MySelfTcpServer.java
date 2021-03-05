package com.example.ch03server.tcpServer;

import com.example.ch03server.pojo.MyTcpClientPOJO;
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
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *     TCPSERVER 文件的服务端,
 * 字节手写，主要用于接收客户端上传单位文件信息
 */
@Component
public class MySelfTcpServer {

    private final static Logger LOGGER = LoggerFactory.getLogger(MySelfTcpServer.class);

    @Autowired
    private Environment environment;

    //用于接收文件的Tcp服务器，字节手写的
    public void mySelftTcpServer() {
        //用于保存对应的通道和对应的文件信息
        Map<SocketChannel, MyTcpClientPOJO> channelMap = new HashMap<SocketChannel, MyTcpClientPOJO>();
        //设置服务器相关信息
        String serverIp = environment.getProperty("tcp.socket.server.ip", "");
        String serverPort = environment.getProperty("tcp.socket.server.port", "");
        //设置服务
        if (StringUtils.isBlank(serverIp) || StringUtils.isBlank(serverPort)) {
            LOGGER.warn("服务器设置错误");
            return;
        }
        //上传目录
        String fullPrefPath = environment.getProperty("tcp.socket.file.path", "");

        //获取选择器
        Selector selector = null;
        //设置服务器
        ServerSocketChannel serverSocketChannel = null;
        try {
            //设置服务器非阻塞，已经对应的监听目录
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)));
            //选择器
            selector = Selector.open();
            //通道注册,这里只是关心可连接的操作
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //文件的前缀
            if (StringUtils.isBlank(fullPrefPath))
                fullPrefPath = ResourceUtils.getURL("classpath:").getPath();
            //具体的调用方法
            HandlerAllChannel(channelMap, fullPrefPath, selector);
        } catch (Exception e) {
            LOGGER.error("服务器出现错误", e);
        } finally {
            IOUtils.closeQuietly(selector);
            IOUtils.closeQuietly(serverSocketChannel);
            //遍历关闭所有的Map中流
            for (SocketChannel socketChannel : channelMap.keySet()) {
                MyTcpClientPOJO myTcpClientPOJO = channelMap.get(socketChannel);
                if (myTcpClientPOJO != null)
                    myTcpClientPOJO.closeFile();
            }
        }
    }

    //具体的用于处理服务器端连接的命令
    private void HandlerAllChannel(Map<SocketChannel, MyTcpClientPOJO> channelMap, String fullPrefPath, Selector selector) throws Exception {
        //服务器每次读取的文件大小
        int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));

        //判断是否已将获取完毕
        while (selector.select() > 0) {
            //每个事件都需要处理
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            //轮询处理
            while (iterator.hasNext()) {
                //获取对应的Key
                SelectionKey readyKey = iterator.next();
                //对IO就绪事件进行判断
                if (readyKey.isAcceptable()) {
                    LOGGER.info("某个客户端连接已经被接受,准备将其注册到可读的IO事件中");
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) readyKey.channel();
                    SocketChannel acceptChannel = serverSocketChannel.accept();
                    acceptChannel.configureBlocking(false);
                    acceptChannel.register(selector, SelectionKey.OP_READ);
                } else if (readyKey.isReadable()) {
                    LOGGER.info("服务器端的读事件准备就绪");
                    //获取读通道
                    SocketChannel readChannel = (SocketChannel) readyKey.channel();
                    //准备读文件
                    ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
                    if (!channelMap.containsKey(readChannel))
                        channelMap.put(readChannel, new MyTcpClientPOJO());
                    MyTcpClientPOJO myTcpClientPOJO = channelMap.get(readChannel);
                    //判断文件名，大小是否已经存在
                    if (StringUtils.isBlank(myTcpClientPOJO.getName())) {
                        readChannel.read(byteBuffer);
                        //模式转换
                        byteBuffer.flip();
                        byte[] array = byteBuffer.array();
                        String fileName = StringUtils.trim(new String(array, "utf-8"));
                        byteBuffer.clear();
                        LOGGER.info("获取到的文件名为:" + fileName);
                        //
                        File file = new File(fullPrefPath + File.separator + fileName);
                        if (file.exists())
                            file.delete();
                        file.createNewFile();
                        //设置文件名
                        myTcpClientPOJO.setName(fileName);
                        myTcpClientPOJO.setFileOutputStream(new FileOutputStream(file));
                        myTcpClientPOJO.setFileChannel(myTcpClientPOJO.getFileOutputStream().getChannel());

                    } else if (myTcpClientPOJO.getSize() < 0) {
                        readChannel.read(byteBuffer);
                        //模式转换
                        byteBuffer.flip();
                        byte[] array = byteBuffer.array();
                        String fileLength = StringUtils.trim(new String(array, "utf-8"));
                        byteBuffer.clear();
                        LOGGER.info("获取到的文件长度为:" + fileLength);
                        myTcpClientPOJO.setSize(Long.valueOf(fileLength));
                    } else if (myTcpClientPOJO.getCurrentSize() < myTcpClientPOJO.getSize()) {
                        int postion = 0;
                        //文件读取
                        while ((postion = readChannel.read(byteBuffer)) > 0) {
                            //模式转换已经还原
                            byteBuffer.flip();
                            myTcpClientPOJO.getFileChannel().write(byteBuffer);
                            byteBuffer.clear();
                            myTcpClientPOJO.setCurrentSize(myTcpClientPOJO.getCurrentSize() + postion);
                            LOGGER.info("接受长度/总长度:" + myTcpClientPOJO.getCurrentSize() + "/" + myTcpClientPOJO.getSize());
                        }

                        if (myTcpClientPOJO.getCurrentSize() == myTcpClientPOJO.getSize()) {
                            LOGGER.info("文件已经接受完成");
                            myTcpClientPOJO.closeFile();
                            channelMap.remove(readChannel);
                        }
                    }

                } else if (readyKey.isWritable()) {
                    LOGGER.info("服务器端的写事件准备就绪");
                }
                //一定要移除对应Key
                iterator.remove();
            }
        }
    }
}
