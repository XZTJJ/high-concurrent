package org.example.GuavaThread;

import com.google.common.util.concurrent.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 使用谷歌的异步线程
 *  自己写的
 */
public class GuavaThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuavaThread.class);

    //对应对应的状态
    private static volatile boolean isWater = false;
    private static volatile boolean isClean = false;

    //开启主线程
    public static void main(String[] args) {
        drinkWater();
    }

    //喝茶流程
    private static void drinkWater() {
        ListeningExecutorService listeningExecutorService = null;
        ExecutorService executorService = null;
        try {
            //准备开始喝茶
            LOGGER.info("准备开始喝茶......");
            //创建对应的线程
            executorService = Executors.newFixedThreadPool(10, new MyThreadFactory("喝茶线程"));
            //转变成Google对应的监听线程
            listeningExecutorService = MoreExecutors.listeningDecorator(executorService);
            //锁对象
            CountDownLatch countDownLatch = new CountDownLatch(2);
            //开始用了
            ListenableFuture<Boolean> waterC = listeningExecutorService.submit(new WaterCallable(10));
            ListenableFuture<Boolean> cleanC = listeningExecutorService.submit(new CleanCallable(5));
            Futures.addCallback(waterC, new MyFutureCallback("烧水", countDownLatch), listeningExecutorService);
            Futures.addCallback(cleanC, new MyFutureCallback("清洗", countDownLatch), listeningExecutorService);

            //关闭线程池
            countDownLatch.await();
        } catch (Exception e) {
            LOGGER.error("泡茶线程失败", e);
        } finally {
            listeningExecutorService.shutdownNow();
            executorService.shutdownNow();
        }
    }

    private static void canDrink() {
        if (isWater && isClean) {
            LOGGER.info("所有工作都做完了,可以喝茶了~~~~~~~");
            isWater = false;
        } else if (isWater && !isClean) {
            LOGGER.info("清洗工作还没有就绪~~~~~~~~");
        } else if (!isWater && isClean) {
            LOGGER.info("烧水工作还没有就绪~~~~~~~~");
        } else {
            LOGGER.info("清洗，烧水工作都没有就绪~~~~~~~");
        }
    }

    //回调任务
    private static class MyFutureCallback implements FutureCallback<Boolean> {
        //查看是那个任务的
        private String name;
        private CountDownLatch countDownLatch;

        public MyFutureCallback(String name, CountDownLatch countDownLatch) {
            this.name = name;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void onSuccess(Boolean result) {
            if (StringUtils.equals(this.name, "烧水"))
                isWater = result;
            else
                isClean = result;
            canDrink();
            countDownLatch.countDown();
        }

        @Override
        public void onFailure(Throwable t) {
            if (StringUtils.equals(this.name, "烧水"))
                isWater = false;
            else
                isClean = false;
            canDrink();
            countDownLatch.countDown();
        }
    }

    //烧水任务,确定对应烧水的步骤
    private static class WaterCallable implements Callable<Boolean> {
        int sleepTime;

        public WaterCallable(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public Boolean call() throws Exception {
            String name = Thread.currentThread().getName();
            try {
                LOGGER.info(name + " : 洗好水壶");
                LOGGER.info(name + " : 灌好凉水");
                LOGGER.info(name + " : 放在火上");
                LOGGER.info(name + " : 处理完成");
                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (Exception e) {
                LOGGER.error(name, e);
                return false;
            }
            return true;
        }
    }


    //清理任务,确定对应烧水的步骤
    private static class CleanCallable implements Callable<Boolean> {
        int sleepTime;

        public CleanCallable(int sleepTime) {
            this.sleepTime = sleepTime;
        }

        @Override
        public Boolean call() throws Exception {
            String name = Thread.currentThread().getName();
            try {
                LOGGER.info(name + " : 洗茶杯");
                LOGGER.info(name + " : 洗茶壶");
                LOGGER.info(name + " : 处理完成");
                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (Exception e) {
                LOGGER.error(name, e);
                return false;
            }
            return true;
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
