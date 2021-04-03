package org.example.GuavaThread;

import com.google.common.util.concurrent.*;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 书本上的
 *  给的 Guava 使用示例
 *  同样是用于烧水的示例
 */
public class GuavaThreadPro {
    private static final Logger Logger = LoggerFactory.getLogger(GuavaThreadPro.class);

    //休眠时间
    public static final int SLEEP_GAP = 500;

    //获取线程名
    public static String getCurThreadName() {
        return Thread.currentThread().getName();
    }

    //对应的任务名称
    private static class HotWarterJob implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception //②
        {

            try {
                Logger.info("洗好水壶");
                Logger.info("灌上凉水");
                Logger.info("放在火上");

                //线程睡眠一段时间，代表烧水中
                Thread.sleep(SLEEP_GAP);
                Logger.info("水开了");

            } catch (InterruptedException e) {
                Logger.info(" 发生异常被中断.");
                return false;
            }
            Logger.info(" 烧水工作，运行结束.");

            return true;
        }
    }

    private static class WashJob implements Callable<Boolean> {

        @Override
        public Boolean call() throws Exception {


            try {
                Logger.info("洗茶壶");
                Logger.info("洗茶杯");
                Logger.info("拿茶叶");
                //线程睡眠一段时间，代表清洗中
                Thread.sleep(SLEEP_GAP);
                Logger.info("洗完了");

            } catch (InterruptedException e) {
                Logger.info(" 清洗工作 发生异常被中断.");
                return false;
            }
            Logger.info(" 清洗工作  运行结束.");
            return true;
        }
    }

    //主线程名称
    private static class MainJob implements Runnable {
        //转态标识
        volatile boolean waterOk = false;
        volatile boolean cupOk = false;
        //休眠
        int gap = SLEEP_GAP / 10;

        //主要流程
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(gap);
                    Logger.info("读书中...");
                } catch (Exception e) {
                    Logger.error(getCurThreadName(), e);
                }
                if (waterOk && cupOk)
                    drinjTea(waterOk, cupOk);
            }
        }

        //喝茶线程
        public void drinjTea(Boolean wOK, Boolean cOK) {
            if (wOK && cOK) {
                Logger.info("泡茶喝，茶喝完");
                this.waterOk = false;
                this.gap = SLEEP_GAP * 100;
            } else if (!wOK) {
                Logger.info("烧水失败，没有茶喝了");
            } else if (!cOK) {
                Logger.info("杯子洗不了，没有茶喝了");
            }
        }
    }

    //主线程
    public static void main(String[] args) {
        //创建主线程示例
        MainJob mainJob = new MainJob();
        new Thread(mainJob, "主线程").start();

        //烧水的逻辑
        Callable<Boolean> hotJob = new HotWarterJob();
        //清洗逻辑
        Callable<Boolean> washJob = new WashJob();
        //创建线程
        ExecutorService jpool = Executors.newFixedThreadPool(10);
        ListeningExecutorService gPool = MoreExecutors.listeningDecorator(jpool);

        //任务提交
        ListenableFuture<Boolean> hotFuture = gPool.submit(hotJob);
        Futures.addCallback(hotFuture, new FutureCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (aBoolean)
                    mainJob.waterOk = true;
            }

            @Override
            public void onFailure(Throwable throwable) {
                Logger.error("烧水失败", throwable);
            }
        });
        //提交清洗的业务逻辑，取到异步任务

        ListenableFuture<Boolean> washFuture = gPool.submit(washJob);
        //绑定任务执行完成后的回调，到异步任务
        Futures.addCallback(washFuture, new FutureCallback<Boolean>() {
            public void onSuccess(Boolean r) {
                if (r) {
                    mainJob.cupOk = true;
                }
            }

            public void onFailure(Throwable t) {
                Logger.info("杯子洗不了，没有茶喝了");
            }
        });
    }
}
