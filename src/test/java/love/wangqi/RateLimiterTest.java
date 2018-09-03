package love.wangqi;

import love.wangqi.tokenbucket.RateLimiter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午8:21
 */
public class RateLimiterTest {
    final static Logger logger = LoggerFactory.getLogger(RateLimiterTest.class);

    @Test
    public void test01() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.create("smooth_ratelimiter", 10.0);

        class Statistics {
            long start = 0;
            long count = 0;

            public synchronized void success() {
                if (start == 0) {
                    start = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - start <= 1000) {
                    count++;
                } else {
                    System.out.println("count " + count);
                    start = System.currentTimeMillis();
                    count = 0;
                }
            }
        }

        Statistics statistics = new Statistics();

        class MyRun implements Runnable {
            @Override
            public void run() {
                boolean acquire;
                do {
                    acquire = rateLimiter.tryAcquire();
                    if (acquire) {
                        logger.info("running... wait " + acquire);
                        statistics.success();
                    }
                } while (!acquire);
            }
        }

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(new MyRun()));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }

        Thread.sleep(3000);
        System.out.println("=====================");
        threadList.clear();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(new MyRun()));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }

    @Test
    public void test02() throws InterruptedException {
        RateLimiter rateLimiter = RateLimiter.create("smooth_ratelimiter", 1.0);
        class MyRun implements Runnable {
            @Override
            public void run() {
                logger.info("running... wait " + rateLimiter.acquire());
            }
        }

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(new MyRun()));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }

    @Test
    public void test03() {
        RateLimiter rateLimiter = RateLimiter.create("smooth_ratelimiter", 1.0);
        Arrays.asList(6, 2, 6).forEach(num -> System.out.println(System.currentTimeMillis() + " wait " + rateLimiter.acquire(num)));
    }
}
