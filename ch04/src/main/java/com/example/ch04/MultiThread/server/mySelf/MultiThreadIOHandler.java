package com.example.ch04.MultiThread.server.mySelf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 多线程下，客户端的事件处理函数,负责处理对应的
 * IO等事件
 */
public class MultiThreadIOHandler implements Runnable {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadIOHandler.class);

    //客户端选在键
    private SelectionKey clientKey;
    //客户端回显用，用线程安全的队列保存
    private ConcurrentLinkedQueue<String> concurrentLinkedQueue = new ConcurrentLinkedQueue<String>();
    //用于限制交流次数使用
    private AtomicInteger countLimit = new AtomicInteger(0);

    //构造函数
    private MultiThreadIOHandler() {
    }

    //提供创建实例的过程
    public static MultiThreadIOHandler getInstance(Selector clientSeletor, SocketChannel clientSocketChannel) throws Exception {
        MultiThreadIOHandler multiThreadIOHandler = new MultiThreadIOHandler();
        clientSocketChannel.configureBlocking(false);
        //clientSeletor.wakeup();
        multiThreadIOHandler.clientKey = clientSocketChannel.register(clientSeletor, SelectionKey.OP_READ);
        multiThreadIOHandler.clientKey.attach(multiThreadIOHandler);
        return multiThreadIOHandler;
    }

    //运行方法
    @Override
    public void run() {
        try {
            if (this.clientKey == null) {
                LOG.info("选择集为空，服务端直接返回");
                return;
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(2048);
            //获取通道
            SocketChannel clientChannel = (SocketChannel) clientKey.channel();
            //进行读写事件的判断
            if (this.clientKey.isReadable()) {
                byteBuffer.clear();
                //获取读取的内容
                int length = 0;
                //对应的消息数组
                byte[] allContents = null;
                while ((length = clientChannel.read(byteBuffer)) > 0) {
                    allContents = ArrayUtils.addAll(allContents,
                            ArrayUtils.subarray(byteBuffer.array(), 0, length));
                    byteBuffer.clear();
                }
                //处理客户端关闭问题时,发送的关闭通知
                if (allContents == null)
                    return;
                String contentStr = new String(allContents);
                //放入队列，由后面处理
                concurrentLinkedQueue.add(contentStr);
                clientKey.interestOps(SelectionKey.OP_WRITE);
                LOG.info("服务端接消息 :" + contentStr);
            } else if (this.clientKey.isWritable()) {
                //从消息队列中获取
                String jsonStr = this.concurrentLinkedQueue.poll();
                //为空的话，直接返回
                if (StringUtils.isBlank(jsonStr))
                    return;
                //解析数据
                JSONObject jsonObject = JSON.parseObject(jsonStr);
                //获取限制字数
                int limitCount = jsonObject.getInteger("limitCount");
                //获取序号
                int order = jsonObject.getInteger("order");
                //获取内容
                String content = jsonObject.getString("content");
                content = StringUtils.replace(content, "客户端", "服务端");

                //服务端回复消息
                JSONObject anserJson = new JSONObject();
                anserJson.put("limitCount", limitCount);
                anserJson.put("order", order);
                anserJson.put("content", content);

                byte[] anserBytes = anserJson.toString().getBytes();
                int pos = 0;
                while (pos < anserBytes.length) {
                    byteBuffer.clear();
                    int length = (anserBytes.length - pos) > 1024 ? 1024 : (anserBytes.length - pos);
                    byteBuffer.put(anserBytes, pos, length);
                    byteBuffer.flip();
                    clientChannel.write(byteBuffer);
                    pos += length;
                }
                //统计发送次数
                int currentCount = countLimit.addAndGet(1);
                //修改选择器事件
                clientKey.interestOps(SelectionKey.OP_READ);
                LOG.info("服务端发消息 : " + anserJson.toString());
                TimeUnit.MILLISECONDS.sleep(5000);
                if (currentCount == limitCount) {
                    LOG.info("服务端已经处理完成客户端数据，准备断开连接");
                    clientKey.cancel();
                }
            } else {

            }
        } catch (Exception e) {
            LOG.error("具体的IO事件处理失败", e);
        }
    }
}
