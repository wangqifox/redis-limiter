package love.wangqi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:50
 */
public class Action {
    private static final Logger logger = LoggerFactory.getLogger(Action.class);

    private RateLimiter rateLimiter;

    public Action() throws Exception {
        rateLimiter = new RateLimiter("limiter", 1, 1 * 1000L);
    }

    public void doSomething() throws Exception {
        while (true) {
            if (rateLimiter.acquire(1)) {
                logger.info("do something");
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Action action = new Action();

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
