package com.example.ch04.SingleThread.server.myself;

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
import java.util.Iterator;
import java.util.Set;

/**
 * 自己手写的单线程版 reactor反应模型
 *  主要用于回显服务
 */
@Component
public class MySelfReactor {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(MySelfReactor.class);
    //环境变量
    @Autowired
    private Environment environment;

    //入口方法
    public void startedServer() {
        //服务器通道
        ServerSocketChannel serverSocketChannel = null;
        //选择器
        Selector selector = null;
        try {
            serverSocketChannel = initServerSocketChannel();
            selector = initSelector();
            runServer(serverSocketChannel, selector);
        } catch (Exception e) {
            LOG.info("服务器端出现错误");
        } finally {
            LOG.info("服务端准备关闭连接");
            IOUtils.closeQuietly(selector);
            IOUtils.closeQuietly(serverSocketChannel);
        }
    }

    //服务器通道初始化
    private ServerSocketChannel initServerSocketChannel() throws Exception {
        //ip和端口设置
        String serverIP = environment.getProperty("reactor.socket.communcation.ip");
        String serverPort = environment.getProperty("reactor.socket.communcation.port");
        if (StringUtils.isBlank(serverIP) || StringUtils.isBlank(serverPort))
            throw new RuntimeException("服务器配置出现问题...");
        //服务器端的创建，非阻塞设置，已经监听信息的绑定和设置
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));

        return serverSocketChannel;
    }

    //选择器的初始化
    private Selector initSelector() throws Exception {
        Selector selector = Selector.open();
        return selector;
    }

    //服务器正式启动
    private void runServer(ServerSocketChannel serverSocketChannel, Selector selector) throws Exception {
        //serverSocketChannel注册,并且为选择键绑定对应的dispatch事件分分发器
        SelectionKey register = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        //注册对应的分发器
        int bufferSize = Integer.valueOf(environment.getProperty("reactor.socket.readSize", "1024"));
        //限制通信次数
        int limitCount = Integer.valueOf(environment.getProperty("reactor.socket.communcation.count", "2"));
        register.attach(new MyDispatchHandler(register, bufferSize, limitCount));
        //服务器还是一直处于服务状态的
        while (selector.select() > 0) {
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                dispatch(iterator.next());
                //移除对应的键
                iterator.remove();
            }
        }
    }

    //处理事件
    private void dispatch(SelectionKey readyKey) throws Exception {
        Runnable attachment = (Runnable) readyKey.attachment();
        if (attachment != null)
            attachment.run();
    }
}
