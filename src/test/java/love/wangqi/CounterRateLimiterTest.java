package love.wangqi;

import love.wangqi.counter.CounterRateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:50
 */
public class CounterRateLimiterTest {
    private static final Logger logger = LoggerFactory.getLogger(CounterRateLimiterTest.class);

    private CounterRateLimiter counterRateLimiter;

    public CounterRateLimiterTest() throws Exception {
        counterRateLimiter = new CounterRateLimiter("limiter", 1, 1 * 1000L);
    }

    public void doSomething() throws Exception {
        while (true) {
            if (counterRateLimiter.acquire(1)) {
                logger.info("do something");
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        CounterRateLimiterTest action = new CounterRateLimiterTest();

        List<Thread> threadList = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadList.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        action.doSomething();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
        for (Thread thread : threadList) {
            thread.start();
        }
        for (Thread thread : threadList) {
            thread.join();
        }
    }
}
