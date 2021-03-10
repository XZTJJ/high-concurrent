package com.example.ch04.MultiThread.server.mySelf;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * 自己手写 : 多线程版本的Reactor反应模式，
 *  单线程存在的问题 ： 并发说太多的话，程序处理不完来
 *  容易造成卡顿。
 *  使用多线程的一些改进的地方 :
 *    1. 服务器端接受通道 和 客户端通道不在使用同一个选择器实例
 *    2. 使用线程池来处理非常消耗客户端业务处理逻辑
 */
@Component
public class MultiThreadReactor {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadReactor.class);
    //环境变量
    @Autowired
    private Environment environment;
    //定时任务
    @Autowired
    private MultiThreadJobsHandlerClass multiThreadJobsHandlerClass;
    //考虑线程安全性的问题，channel 和 select 就不使用成员变量

    public void initAndRun() {
        Selector[] selectors = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            //首先申明两个选择器
            selectors = initSelector();
            //服务端的连接
            serverSocketChannel = initServerSocket();
            //服务端的注册和运行
            runServerSocket(selectors, serverSocketChannel);

        } catch (Exception e) {
            LOG.error("服务器端连接出现问题", e);
        } finally {
            //关闭对应的连接
            for (Selector s : selectors)
                IOUtils.closeQuietly(s);
            IOUtils.closeQuietly(serverSocketChannel);
        }
    }

    //初始化选择器
    private Selector[] initSelector() throws Exception {
        return new Selector[]{Selector.open(), Selector.open()};
    }

    //初始化服务端
    private ServerSocketChannel initServerSocket() throws Exception {
        //ip和端口设置
        String serverIP = environment.getProperty("reactor.socket.communcation.ip");
        String serverPort = environment.getProperty("reactor.socket.communcation.port");
        if (StringUtils.isBlank(serverIP) || StringUtils.isBlank(serverPort))
            throw new RuntimeException("服务器配置出现问题...");
        //服务器端的创建，非阻塞设置，已经监听信息的绑定和设置
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
        //返回连接实例
        return serverSocketChannel;
    }

    //服务端运行实例方法,
    private void runServerSocket(Selector[] selectors, ServerSocketChannel serverSocketChannel) throws Exception {
        LOG.info("服务端启动成功~~~~");
        //服务端注册
        SelectionKey serverKey = serverSocketChannel.register(selectors[0], SelectionKey.OP_ACCEPT);
        //创建处理运行时Obj方法
        serverKey.attach(new MultThreadDispatch(selectors[1], serverKey));
        //运行任务处理器
        this.multiThreadJobsHandlerClass.runJobs(selectors);
        while (true) {
            TimeUnit.MINUTES.sleep(10);
        }
    }


}
