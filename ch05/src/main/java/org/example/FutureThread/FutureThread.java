package org.example.FutureThread;

import org.example.OriThread.OriThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 使用有返回值的任务类型
 *  FutureTask
 */
public class FutureThread {
    //日志
    private static final Logger LOGGER = LoggerFactory.getLogger(FutureThread.class);

    public static void main(String[] args) {
        try {
            LOGGER.info("主线程准备开启烧水线程");
            LOGGER.info("主线程准备开启清洗线程");
            LOGGER.info("主线程等待分支线程完成任务");
            //声明烧水和清洗线程
            FutureTask<Boolean> water = new FutureTask<Boolean>(new WaterCallable(10));
            new MyThreadFactory("烧水线程").newThread(water).start();
            FutureTask<Boolean> clean = new FutureTask<Boolean>(new WaterCallable(10));
            new MyThreadFactory("清洗线程").newThread(clean).start();
            Boolean isWater = water.get();
            Boolean isClean = clean.get();
            if (isClean && isWater)
                LOGGER.info("主线程在喝茶了.........");
            else
                LOGGER.info("烧水或者清洗失败");
        } catch (Exception e) {
            LOGGER.error("泡茶过程出现错误", e);
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

        public MyThreadFactory(String name) {
            this.name = name;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(name);
            thread.setDaemon(false);
            return thread;
        }
    }

}
