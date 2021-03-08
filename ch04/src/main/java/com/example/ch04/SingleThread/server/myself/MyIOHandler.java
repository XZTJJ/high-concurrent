package com.example.ch04.SingleThread.server.myself;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

//具体的IO操作事件
public class MyIOHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MyDispatchHandler.class);
    //反应区
    private ByteBuffer byteBuffer;
    //选择键
    private SelectionKey sk;
    //缓冲区大小
    private int size = 0;
    //上一次发的小心内容
    private String content;
    //接受次数后，停止接受
    private int limitCount;
    //通信次数
    private int count;

    public MyIOHandler(SelectionKey sk, int size, int limitCount) {
        this.sk = sk;
        this.size = size;
        this.limitCount = limitCount;
        byteBuffer = ByteBuffer.allocate(size);
    }

    //具体的IO操作事件
    @Override
    public void run() {
        try {
            //通过readKey获取，通道，注册器
            SocketChannel channel = (SocketChannel) sk.channel();
            byteBuffer.clear();
            //选择敢兴趣的事件
            if (sk.isReadable()) {
                channel.read(byteBuffer);
                content = new String(byteBuffer.array(), 0, byteBuffer.position());
                LOG.info("服务端收消息 : " + content);
                //重新向选择器注册写的事件
                sk.interestOps(SelectionKey.OP_WRITE);
            } else if (sk.isWritable()) {
                LOG.info("服务端发消息 : " + content);
                byteBuffer.put(content.getBytes());
                //转成写模式
                byteBuffer.flip();
                channel.write(byteBuffer);
                sk.interestOps(SelectionKey.OP_READ);
                TimeUnit.MILLISECONDS.sleep(500);
                count++;
            }

            //因为需要循环用到，到次数达到以后，取消通信
            if (count > limitCount) {
                sk.cancel();
            }
        } catch (Exception e) {
            LOG.error("IO详细事件处理失败", e);
        }
    }
}
