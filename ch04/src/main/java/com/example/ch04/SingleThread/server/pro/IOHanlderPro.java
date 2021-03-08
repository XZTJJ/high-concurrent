package com.example.ch04.SingleThread.server.pro;

import com.example.ch04.SingleThread.server.myself.MyDispatchHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

/**
 * 书上给的处理 : 处理具体的 所有客户端连接和业务的所有逻辑
 */
public class IOHanlderPro implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(MyDispatchHandler.class);

    //选择器
    private Selector selector;
    //通道
    private SocketChannel socketChannel;
    //选择键
    private SelectionKey selectionKey;
    //感兴趣的操作
    private final int RECIEVING = 0, SENDING = 1;
    //默认转态下是接受事件
    private int state = RECIEVING;
    //缓冲区
    private ByteBuffer byteBuffer;
    //缓冲区大小
    private int byteSize;

    //私有构造函数，主要为了通道注册,防止this指针逸出
    private IOHanlderPro(Selector selector, SocketChannel socketChannel, int byteSize) {
        this.selector = selector;
        this.socketChannel = socketChannel;
        this.byteSize = byteSize;
        this.byteBuffer = ByteBuffer.allocate(this.byteSize);
    }

    public static IOHanlderPro getInstance(Selector selector, SocketChannel socketChannel, int byteSize) throws Exception {
        //获取对象实例
        IOHanlderPro ioHanlderPro = new IOHanlderPro(selector, socketChannel, byteSize);
        //通道注册,并且设置非阻塞
        ioHanlderPro.socketChannel.configureBlocking(false);
        ioHanlderPro.selectionKey = ioHanlderPro.socketChannel.register(ioHanlderPro.selector, 0);
        //设置选择键的处理hanlder
        ioHanlderPro.selectionKey .attach(ioHanlderPro);
        //选在通道感兴趣的IO事件
        ioHanlderPro.selectionKey .interestOps(SelectionKey.OP_READ);
        //主要的作用应该使注册的不感兴趣的事情立即返回，从而能马上获取读事件
        selector.wakeup();
        return ioHanlderPro;
    }

    //方法的复写，主要是处理方法
    @Override
    public void run() {
        try {
            //处理服务器接受转态下的逻辑
            if (this.state == this.RECIEVING) {
                //处理写的逻辑
                int length = 0;
                while ((length = this.socketChannel.read(this.byteBuffer)) > 0) {
                    LOG.info("服务端收消息 : " + (new String(this.byteBuffer.array(), 0, length)));
                }
                //改变标志 和 感兴趣的IO事件
                this.byteBuffer.flip();
                this.selectionKey.interestOps(SelectionKey.OP_WRITE);
                this.state = this.SENDING;
            } else if (this.state == this.SENDING) {
                //返回收到的消消息,并且改变标志位
                this.socketChannel.write(byteBuffer);
                this.selectionKey.interestOps(SelectionKey.OP_READ);
                this.state = this.RECIEVING;
                //发送消息的处理逻辑
                LOG.info("服务端发消息 : " + (new String(this.byteBuffer.array(), 0, this.byteBuffer.position())));
                this.byteBuffer.clear();
                TimeUnit.MILLISECONDS.sleep(2000);
            }
        } catch (Exception e) {
            LOG.error("处理客户端连接错误", e);
        }
    }
}
