package love.wangqi.counter;

import love.wangqi.RedisPool;
import love.wangqi.ScriptUtil;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:40
 */
public class CounterRateLimiter extends RedisPool {
    private String script;
    private String key;
    private int maxPermits;
    private Long intervalMilliseconds;

    public CounterRateLimiter(String key, int maxPermits, Long intervalMilliseconds) throws Exception {
        script = ScriptUtil.getScript("counter_limit.lua");
        this.key = key;
        this.maxPermits = maxPermits;
        this.intervalMilliseconds = intervalMilliseconds;
    }

    public Boolean acquire(int permits) throws Exception {
        Jedis redis = getJedis();
        try {
            Object result = redis.eval(script,
                    Arrays.asList(key, String.valueOf(maxPermits), String.valueOf(intervalMilliseconds)),
                    Arrays.asList(String.valueOf(permits))
            );
            if (result != null && 0 != (Long) result) {
                return true;
            } else {
                return false;
            }
        } finally {
            returnJedis(redis);
        }
    }
}
