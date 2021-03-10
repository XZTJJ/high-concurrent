package com.example.ch04.MultiThread.server.mySelf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

//程序的处理线程，负责查询IO事件，处理客户端事件等事件
@Component
public class MultiThreadJobsHandlerClass {
    private static final Logger LOG = LoggerFactory.getLogger(MultiThreadIOHandler.class);
    //线程工厂
    //任务调度线程池
    private final ExecutorService queryThreadPool = Executors.newFixedThreadPool(2, new MyThreadFactory("查询线程"));
    //客户端任务处理线程
    private final ExecutorService clientThreadPool = Executors.newFixedThreadPool(5, new MyThreadFactory("任务线程"));

    //处理线程重复处理问题情况,针对客户端的
    private ConcurrentMap<SelectableChannel, Integer> concurrentMap = new ConcurrentHashMap<SelectableChannel, Integer>();

    //处理任务
    public void runJobs(Selector[] selectors) throws Exception {
        queryThreadPool.execute(new StartJobs(selectors[0], 0));
        queryThreadPool.execute(new StartJobs(selectors[1], 1));
    }

    //处理所有任务的线程
    class StartJobs implements Runnable {
        //选择器
        private Selector selector;
        //标志选择器类型，0表示服务器端，1表示客户端
        private final int mark;

        public StartJobs(Selector selector, int mark) {
            this.selector = selector;
            this.mark = mark;
        }

        @Override
        public void run() {
            try {
                //一直循环处理
                while (!Thread.interrupted()) {
                    int select = selector.select(100);
                    if (select <= 0)
                        continue;
                    //选择集
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = selectionKeys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey readyKey = iterator.next();
                        Runnable task = (Runnable) readyKey.attachment();
                        //有可能为空，所以加上null判断
                        if (task == null)
                            continue;
                        //区分服务端和客户端的处理逻辑
                        if (mark == 0) {
                            task.run();
                        } else {
                            //判断键是否被取消了
                            if (!readyKey.isValid())
                                continue;
                            //排重,只留下一个线程处理连接就行
                            int opsValue = readyKey.readyOps();
                            SelectableChannel channel = readyKey.channel();
                            concurrentMap.putIfAbsent(channel, -1);
                            Integer oldValue = concurrentMap.replace(channel, opsValue);
                            if (opsValue == oldValue)
                                continue;
                            MultiThreadJobsHandlerClass.this.clientThreadPool.execute(task);
                        }
                    }
                    selectionKeys.clear();
                }
            } catch (Exception e) {
                LOG.error("定时任务" + mark + "运行错误", e);
            }
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
