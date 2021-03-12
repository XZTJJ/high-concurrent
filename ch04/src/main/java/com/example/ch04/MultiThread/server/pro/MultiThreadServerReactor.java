package com.example.ch04.MultiThread.server.pro;


import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 书上的 ， 多线程版本的Reactor反应模式
 *  感觉和自己写的各有所长吧
 */
@Component
public class MultiThreadServerReactor {
    //日志
    private final static Logger LOG = LoggerFactory.getLogger(MultiThreadServerReactor.class);
    //端口和IP
    String serverIP;
    String serverPort;
    //选择器
    private Selector[] selectors;
    //通道
    private ServerSocketChannel serverSocketChannel;
    //计数器
    private AtomicInteger atomicInteger;
    //任务处理线程类
    private SubReactor[] subReactors;

    public MultiThreadServerReactor() throws Exception {
        atomicInteger = new AtomicInteger(0);
        //获取端口等信息
        //ip和端口设置
        ResourceBundle resource = ResourceBundle.getBundle("application");
        serverIP = resource.getString("reactor.socket.communcation.ip");
        serverPort = resource.getString("reactor.socket.communcation.port");
        if (StringUtils.isBlank(serverIP) || StringUtils.isBlank(serverPort))
            throw new RuntimeException("服务器配置出现问题...");
        //选择器处理
        selectors = new Selector[]{Selector.open(), Selector.open()};
        //服务器通道创建
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
        serverSocketChannel.configureBlocking(false);
        //通道注册
        SelectionKey sk = serverSocketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
        //给分配键选择处理的类
        sk.attach(new AcceptorHandler());
        //创建处理线程
        subReactors = new SubReactor[]{new SubReactor(selectors[0]), new SubReactor(selectors[1])};
    }

    //启动服务器
    public void startService() {
        LOG.info("服务端启动成功~~~");
        Thread thread0 = new Thread(subReactors[0]);
        thread0.setName("查询线程0");
        thread0.setDaemon(false);
        thread0.start();
        Thread thread1 = new Thread(subReactors[1]);
        thread1.setName("查询线程1");
        thread1.setDaemon(false);
        thread1.start();


        while (true){
            try {
                TimeUnit.MINUTES.sleep(10);
            } catch (InterruptedException e) {
                LOG.error("休眠失败",e);
            }
        }
    }


    //选择器处理任务
    private class SubReactor implements Runnable {
        //选择器
        private Selector selector;

        //构造器
        public SubReactor(Selector selector) {
            this.selector = selector;
        }

        @Override
        public void run() {
            try {
                //一直循环
                while (!Thread.interrupted()) {
                    //调用选择器查询
                    selector.select() ;
                    //获取选择集
                    Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    //循环处理
                    while (iterator.hasNext()) {
                        //因为选在器是一直阻塞，所以不会存在选择集为空的情况
                        SelectionKey sk = iterator.next();
                        //处理任务
                        dispatch(sk);
                    }
                    //清空选择集
                    selectionKeys.clear();
                }
            } catch (Exception e) {
                LOG.error("处理线程出现错误", e);
            }
        }

        //分配任务
        public void dispatch(SelectionKey sk) {
            //高并发下存在可见性问题，所以需要判断
            Runnable task = (Runnable) sk.attachment();
            if (task != null)
                task.run();
        }
    }

    //服务端任务处理类,只是一个简单的Dispatch作用
    private class AcceptorHandler implements Runnable {
        @Override
        public void run() {
            try {
                //只是简单的获取客户端连接，并未客户端连接分配处理事件
                SocketChannel socketChannel = MultiThreadServerReactor.this.serverSocketChannel.accept();
                //如果socketChannel为空，则直接不处理,在客户端处理线程处理处理操作
                if (socketChannel != null)
                    new MultiThreadHandler(selectors[atomicInteger.get()], socketChannel);
                //处理数组越界的问题
                if (atomicInteger.incrementAndGet() == selectors.length)
                    atomicInteger.set(0);
            } catch (Exception e) {
                LOG.error("服务端Dispatch出现错误", e);
            }
        }
    }
}
