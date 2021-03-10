package com.example.ch04.MultiThread.client;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *  多线程的Reactor模式下面的客户端
 */
@Component
public class MultiThreadClient {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadClient.class);
    //环境变量
    @Autowired
    private Environment environment;
    //技术用
    private AtomicInteger counter = new AtomicInteger(0);

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
            //注册的key
            SelectionKey registerKey = socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
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
                        //获取读取的内容
                        int length = 0;
                        byte[] allContents = null;
                        while ((length = readyChannel.read(byteBuffer)) > 0) {
                            allContents = ArrayUtils.addAll(allContents,
                                    ArrayUtils.subarray(byteBuffer.array(), 0, length));
                            byteBuffer.clear();
                        }
                        String contentStr = new String(allContents);
                        LOG.info("客户端收消息 : " + contentStr);
                        isContinue++;
                    } else if (readyKey.isWritable()) {
                        //进行写操作
                        String anserJson = getSendContent(limitCount);
                        byte[] anserBytes = anserJson.getBytes();
                        int pos = 0;
                        while (pos < anserBytes.length) {
                            byteBuffer.clear();
                            int length = (anserBytes.length - pos) > 1024 ? 1024 : (anserBytes.length - pos);
                            byteBuffer.put(anserBytes, pos, length);
                            byteBuffer.flip();
                            readyChannel.write(byteBuffer);
                            pos += length;
                        }
                        LOG.info("客户端发消息 : " + anserJson);
                        TimeUnit.MILLISECONDS.sleep(5000);
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

    //结束标识
    private boolean isNeedClose(int count, int limitCount) {
        return count >= limitCount ? true : false;
    }

    //发送内容
    private String getSendContent(int limitCount) {

        JSONObject sendJson = new JSONObject();
        sendJson.put("limitCount", limitCount);
        sendJson.put("order", counter.addAndGet(1));
        String content = "";
        for (int i = 0; i < 20; i++)
            content += "这是客户端发送信息" + LocalDateTime.now().toString();
        sendJson.put("content", content);
        return sendJson.toString();
    }
}
