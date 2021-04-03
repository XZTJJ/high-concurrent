package org.example.GuavaThread;

import com.google.common.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用谷歌的异步线程,测试用的
 *  自己写的
 */
public class GuavaThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuavaThreadTest.class);


    //开启主线程
    public static void main(String[] args) {
        testMuchTask();
    }

    //喝茶流程
    private static void testMuchTask() {
        ListeningExecutorService listeningExecutorService = null;
        ExecutorService executorService = null;
        try {
            //准备开始喝茶
            LOGGER.info("准备开始喝茶......");
            //创建对应的线程
            executorService = Executors.newFixedThreadPool(10, new MyThreadFactory("测试线程"));
            //转变成Google对应的监听线程
            listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
            int size = 100;
            //锁对象
            CountDownLatch countDownLatch = new CountDownLatch(size);
            for (int i = 0; i < size; i++) {
                ListenableFuture<String> submit = listeningExecutorService.submit(new TestCallable(2));
                Futures.addCallback(submit, new MyFutureCallback(countDownLatch), listeningExecutorService);
            }
            //关闭线程池
            countDownLatch.await();
        } catch (Exception e) {
            LOGGER.error("泡茶线程失败", e);
        } finally {
            listeningExecutorService.shutdownNow();
            executorService.shutdownNow();
        }
    }


    //回调任务
    private static class MyFutureCallback implements FutureCallback<String> {

        private CountDownLatch countDownLatch;

        public MyFutureCallback(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onSuccess(String result) {
            countDownLatch.countDown();
            long count = countDownLatch.getCount();
            LOGGER.info(result + count);

        }

        @Override
        public void onFailure(Throwable t) {
            LOGGER.error("某个任务失败", t);
            countDownLatch.countDown();
        }
    }


    //测试任务,确定对应烧水的步骤
    private static class TestCallable implements Callable<String> {
        int sleepTime;

        public TestCallable(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public String call() throws Exception {
            String name = Thread.currentThread().getName();
            try {

                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (Exception e) {
                LOGGER.error(name, e);
                return name;
            }
            return name + "完成";
        }

    }


    //线程工厂
    private static class MyThreadFactory implements ThreadFactory {
        //测试对象
        private String name;
        private AtomicInteger atomicInteger = new AtomicInteger(0);

        public MyThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(name + atomicInteger.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        }
    }


}
