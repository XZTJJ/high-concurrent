package com.example.ch03server.tcpServer;

import com.example.ch03server.pojo.TcpServerPROPOJO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//书上给的算是标准的服务器接收方法
@Component
public class TcpServerPro {
    //日志
    private final static Logger LOGGER = LoggerFactory.getLogger(TcpServerPro.class);
    //字符集
    private Charset charset = Charset.forName("utf-8");
    @Autowired
    private Environment environment;

    //书上的
    public void tcpServerpro() {
        //用于保存所有的数据对应的客户端集合
        Map<SelectableChannel, TcpServerPROPOJO> clientMap = new HashMap<SelectableChannel, TcpServerPROPOJO>();
        //服务器对应的监控
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
        //选择器
        Selector selector = null;
        //服务端数据流
        ServerSocketChannel serverSocketChannel = null;
        try {
            //注册服务器对应的事件
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(serverIp, Integer.valueOf(serverPort)));
            //选择器,并且注销到对应的选择器上
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //开始选择，选择器开始选择
            while (selector.select() > 0) {
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                //循环迭代
                while (iterator.hasNext()) {
                    SelectionKey readyKey = iterator.next();
                    //接受事件处理函数
                    if (readyKey.isAcceptable())
                        acceptHandler(clientMap, readyKey, selector);
                    else if (readyKey.isReadable())
                        readHandler(clientMap, readyKey);
                    else if (readyKey.isWritable())
                        writeHandler(clientMap, readyKey);
                    //移除对应的key
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            LOGGER.error("服务器端接受文件失败", e);
        } finally {
            IOUtils.closeQuietly(serverSocketChannel);
            IOUtils.closeQuietly(selector);
        }
    }

    //接受事件处理函数
    private void acceptHandler(Map<SelectableChannel, TcpServerPROPOJO> clientMap, SelectionKey readyKey, Selector selector) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) readyKey.channel();
            SocketChannel acceptChannel = serverSocketChannel.accept();
            acceptChannel.configureBlocking(false);
            acceptChannel.register(selector, SelectionKey.OP_READ);
            clientMap.putIfAbsent(acceptChannel, new TcpServerPROPOJO());
            TcpServerPROPOJO tcpServerPROPOJO = clientMap.get(acceptChannel);
            tcpServerPROPOJO.setRemoteAddr((InetSocketAddress) acceptChannel.getRemoteAddress());
            LOGGER.info(tcpServerPROPOJO.getRemoteAddr() + " 已经连接服务器");
        } catch (Exception e) {
            LOGGER.error("服务器接受出现异常", e);
            if (clientMap.containsKey(readyKey))
                clientMap.remove(readyKey);
        }
    }

    //可读事件处理函数
    private void readHandler(Map<SelectableChannel, TcpServerPROPOJO> clientMap, SelectionKey readyKey) {
        //获取对应的数据
        TcpServerPROPOJO tcpServerPROPOJO = clientMap.get(readyKey.channel());
        SocketChannel socketChannel = null;
        try {
            socketChannel = (SocketChannel) readyKey.channel();
            //开始循环读取
            int bufferSize = Integer.valueOf(environment.getProperty("custom.buffer.size", "1024"));
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            //长度
            int length = 0;
            int total = 0;
            while ((length = socketChannel.read(byteBuffer)) > 0) {
                //模式翻转
                byteBuffer.flip();
                //开始处理文件，说明是文件
                if (StringUtils.isBlank(tcpServerPROPOJO.getFileName())) {
                    //文件上传路径
                    String fullPrefPath = environment.getProperty("tcp.socket.file.path", "");
                    if (StringUtils.isBlank(fullPrefPath))
                        fullPrefPath = ResourceUtils.getURL("classpath:").getPath();
                    //文件名
                    String fileName = charset.decode(byteBuffer).toString();
                    //打印日志
                    LOGGER.info(tcpServerPROPOJO.getRemoteAddr() + " 上传的文件名为:" + fileName);
                    //获取文件流
                    File file = new File(fullPrefPath + File.separator + fileName);
                    if (file.exists())
                        file.delete();
                    file.createNewFile();
                    tcpServerPROPOJO.setFileChannel(new FileOutputStream(file).getChannel());
                    tcpServerPROPOJO.setFileName(fileName);
                } else if (tcpServerPROPOJO.getFileSize() < 0) {
                    //获取文件长度
                    long size = byteBuffer.getLong();
                    LOGGER.info(tcpServerPROPOJO.getRemoteAddr() + " 上传的文件长度为:" + size);
                    //
                    tcpServerPROPOJO.setFileSize(size);
                } else {
                    //上传类容
                    tcpServerPROPOJO.getFileChannel().write(byteBuffer);
                    total += length;
                    LOGGER.info(tcpServerPROPOJO.getRemoteAddr() + " 已经长传/总长度:" + total+"/"+tcpServerPROPOJO.getFileSize());
                }
                byteBuffer.clear();
            }

            //处理上传完成的情况
            if (length == -1) {
                //取消注册，并且移除对应的事件
                LOGGER.info(tcpServerPROPOJO.getRemoteAddr() + " 文件上传完成");
                readyKey.cancel();
                IOUtils.closeQuietly(tcpServerPROPOJO.getFileChannel());
                clientMap.remove(readyKey);
            }
        } catch (Exception e) {
            LOGGER.error("服务器端处理可读事件出现异常", e);
            readyKey.cancel();
            IOUtils.closeQuietly(tcpServerPROPOJO.getFileChannel());
            clientMap.remove(readyKey);
        }
    }

    //可写事件处理函数
    private void writeHandler(Map<SelectableChannel, TcpServerPROPOJO> clientMap, SelectionKey readyKey) {
        LOGGER.info("暂时没有可写的事件~~~~~~~");
    }
}
