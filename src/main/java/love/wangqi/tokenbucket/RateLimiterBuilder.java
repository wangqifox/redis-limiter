package love.wangqi.tokenbucket;

import love.wangqi.RedisPool;
import redis.clients.jedis.Jedis;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/3 上午10:45
 */
public class RateLimiterBuilder {
    Jedis jedis;

    public RateLimiterBuilder setJedis(Jedis jedis) {
        this.jedis = jedis;
        return this;
    }

    public RateLimiter create(String key, double permitsPerSecond) {
        return create(key, permitsPerSecond, RateLimiter.SleepingStopwatch.createFromSystemTimer());
    }

    RateLimiter create(String key, double permitsPerSecond, RateLimiter.SleepingStopwatch stopwatch) {
        RateLimiter rateLimiter = new SmoothBurstyImpl(jedis, key, stopwatch, 1.0);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }
}
