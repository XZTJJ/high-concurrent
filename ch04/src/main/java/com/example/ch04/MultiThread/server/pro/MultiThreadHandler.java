package com.example.ch04.MultiThread.server.pro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

//多线程版本的客户端处理任务
public class MultiThreadHandler implements Runnable {
    //日志
    private final static Logger LOG = LoggerFactory.getLogger(MultiThreadHandler.class);
    //需要初始化选择器
    private Selector selector;
    //通道
    private SocketChannel socketChannel;
    //选择集
    private SelectionKey sk;
    //读写标识
    private final static int SENDING = 1, RECIEVING = 0;
    //事件标志,默认是读
    private int state = RECIEVING;
    //初始化线程池
    private final static ExecutorService execPools = Executors.newFixedThreadPool(5, new MyThreadFactory("处理线程"));
    //字节缓冲区
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    //构造函数
    public MultiThreadHandler(Selector selector, SocketChannel socketChannel) throws Exception {
        this.selector = selector;
        this.socketChannel = socketChannel;
        //通道之类的注册
        socketChannel.configureBlocking(false);
        //通道注册
        this.sk = socketChannel.register(this.selector, 0);
        //绑定事件,存在this指针泄露的风险
        this.sk.attach(this);
        //修改感兴趣的事件
        this.sk.interestOps(SelectionKey.OP_READ);
        //因为修改了感兴趣的时间，使得selector中阻塞中恢复过来，选择新的事件
        this.selector.wakeup();
        //存在接受事件
        state = RECIEVING;
    }

    @Override
    public void run() {
        execPools.execute(new AysncTask());
    }

    //具体的任务处理方法，因为是多线程，需要保证方法级别的原子操作
    public synchronized void aysncRun() {
        try {
            /*
             * 一个通道不管是发生什么事件，channel 和
             *     selectKey 被一个注册器注册了，那就是同一个实例
             */
            if (state == RECIEVING) {
                //读取数据,并转成只读模式
                this.socketChannel.read(this.byteBuffer);
                this.byteBuffer.flip();
                //修改注册事件 和 状态标识
                this.sk.interestOps(SelectionKey.OP_WRITE);
                state = SENDING;
            } else if (state == SENDING) {
                //发送数据,并转成写模式
                this.socketChannel.write(this.byteBuffer);
                this.byteBuffer.clear();
                //修改注册事件 和 状态标识
                this.sk.interestOps(SelectionKey.OP_READ);
                state = RECIEVING;
                TimeUnit.MILLISECONDS.sleep(2000);
            }

        } catch (Exception e) {
            LOG.error("客户端任务处理失败", e);
        }
    }

    //创建一个异步任务内
    private class AysncTask implements Runnable {
        @Override
        public void run() {
            //只是负责调用外部的方法
            MultiThreadHandler.this.aysncRun();
        }
    }

    //内部线程工厂
    static class MyThreadFactory implements ThreadFactory {
        //名字和序号
        private String name;
        private AtomicInteger integer;

        public MyThreadFactory(String name) {
            this.name = name;
            this.integer = new AtomicInteger(0);
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(name + integer.getAndIncrement());
            thread.setDaemon(false);
            return thread;
        }
    }

}
