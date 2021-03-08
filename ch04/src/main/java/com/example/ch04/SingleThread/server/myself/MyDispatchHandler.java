package com.example.ch04.SingleThread.server.myself;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 *  reactor反应模式的，IO事件分发器。
 *
 */

public class MyDispatchHandler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MyDispatchHandler.class);

    //选择键
    private SelectionKey sk;
    //缓冲区大小
    private int size = 0;
    //限制的通信次数
    private int limitCount;

    public MyDispatchHandler(SelectionKey sk, int size, int limitCount) {
        this.sk = sk;
        this.size = size;
        this.limitCount = limitCount;
    }

    //事件分发的具体过程
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) sk.channel();
            SocketChannel acceptChannel = serverSocketChannel.accept();
            acceptChannel.configureBlocking(false);
            //注册读事件
            SelectionKey registerKey = acceptChannel.register(sk.selector(), SelectionKey.OP_READ);
            registerKey.attach(new MyIOHandler(registerKey, this.size, this.limitCount));
        } catch (Exception e) {
            LOG.error("事件分配失败", e);
        }
    }

}
