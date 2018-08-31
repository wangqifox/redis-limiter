package love.wangqi;

import love.wangqi.tokenbucket.RateLimiter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        RateLimiter rateLimiter = RateLimiter.create("smooth_ratelimiter", 20.0);
        class MyRun implements Runnable {
            @Override
            public void run() {
                boolean acquire = rateLimiter.tryAcquire();
                if (acquire) {
                    logger.info("running... wait " + acquire);
                }
            }
        }

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            threadList.add(new Thread(new MyRun()));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }
}
