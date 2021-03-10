package com.example.ch04.MultiThread.server.mySelf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 多线程下，服务器端的事件处理函数,其实
 * 就是一个Dispatch的作用而已
 */
public class MultThreadDispatch implements Runnable {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MultThreadDispatch.class);
    //客户端使用的注册器
    private Selector clientSelector;
    //服务器通道对应的选择键
    private SelectionKey serverKey;

    //构造函数的初始化
    public MultThreadDispatch(Selector clientSelector, SelectionKey serverKey) {
        this.clientSelector = clientSelector;
        this.serverKey = serverKey;
    }

    //服务器端的处理时间，通过选择键获取对应对应的通道，选择键之类的
    @Override
    public void run() {
        try {
            //服务器通道实例
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) serverKey.channel();
            //获取客户端通道
            SocketChannel clientSocketChannel = serverSocketChannel.accept();
            //时间注册
            MultiThreadIOHandler.getInstance(clientSelector, clientSocketChannel);
        } catch (Exception e) {
            LOG.error("服务端事件分配失败", e);
        }
    }
}
