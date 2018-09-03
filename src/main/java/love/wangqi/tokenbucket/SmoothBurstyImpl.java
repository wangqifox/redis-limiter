package love.wangqi.tokenbucket;

import redis.clients.jedis.Jedis;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/3 上午10:42
 */
public class SmoothBurstyImpl extends SmoothBursty {
    private Jedis jedis;

    SmoothBurstyImpl(Jedis jedis, String key, RateLimiter.SleepingStopwatch stopwatch, double maxBurstSeconds) {
        super(key, stopwatch, maxBurstSeconds);
        this.jedis = jedis;
    }

    @Override
    protected Jedis getJedis() {
        return jedis;
    }
}
