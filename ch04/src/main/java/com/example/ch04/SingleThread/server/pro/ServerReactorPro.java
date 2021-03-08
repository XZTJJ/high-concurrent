package com.example.ch04.SingleThread.server.pro;

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
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 书上给说的 reactor反应器 样例
 * 感觉写的还不错
 */
@Component
public class ServerReactorPro {
    //日志
    private static final Logger LOG = LoggerFactory.getLogger(ServerReactorPro.class);
    //环境变量
    @Autowired
    private Environment environment;

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;


    //入口方法
    public void startServerReactorPro() {
        try {
            //ip和端口设置
            String serverIP = environment.getProperty("reactor.socket.communcation.ip");
            String serverPort = environment.getProperty("reactor.socket.communcation.port");
            if (StringUtils.isBlank(serverIP) || StringUtils.isBlank(serverPort))
                throw new RuntimeException("服务器配置出现问题...");
            //服务器端的创建，非阻塞设置，已经监听信息的绑定和设置
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress(serverIP, Integer.valueOf(serverPort)));
            //选择器
            selector = Selector.open();
            //获取选择器为通道分配选择键
            SelectionKey register = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //为键分配处理事件,主要是dispatch操作
            register.attach(new AcceptorHandler());

            //轮询感兴趣的时间
            while (selector.select() > 0 || selector.keys().size() > 0) {
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> it = selected.iterator();
                while (it.hasNext()) {
                    //Reactor负责dispatch收到的事件
                    SelectionKey sk = it.next();
                    //分配时间或者实际的处理时间的handler调用
                    dispatch(sk);
                }
                selected.clear();
            }

        } catch (Exception e) {
            LOG.info("服务器端出现错误");
        } finally {
            LOG.info("服务端准备关闭连接");
            IOUtils.closeQuietly(selector);
            IOUtils.closeQuietly(serverSocketChannel);
        }

    }

    //直接调用对应的hanlder，包括客户端和服务端的
    private void dispatch(SelectionKey readyKey) throws Exception {
        Runnable attachment = (Runnable) readyKey.attachment();
        if (attachment != null)
            attachment.run();
    }

    //内部类，相当于一个dispatch作用，给对应的时间找到对应数据
    class AcceptorHandler implements Runnable {
        @Override
        public void run() {
            try {
                int bufferSize = Integer.valueOf(environment.getProperty("reactor.socket.readSize", "1024"));
                //获取对应的客户端连接通道，并设置对应的事件
                SocketChannel accept = serverSocketChannel.accept();
                //为对应的客户端连接设置单独的操作
                if (accept != null)
                    IOHanlderPro.getInstance(selector, accept, bufferSize);
            } catch (Exception e) {
                LOG.error("事件分配失败", e);
            }
        }
    }
}
