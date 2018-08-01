package love.wangqi;

import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:40
 */
public class RateLimiter {
    private GenericObjectPool<Jedis> jedisPool;
    private String script;

    private String key;
    private int maxPermits;
    private Long intervalMilliseconds;

    public RateLimiter(String key, int maxPermits, Long intervalMilliseconds) throws Exception {
        jedisPool = new GenericObjectPool<>(new JedisPooledFactory("localhost", 6379));
        script = ScriptUtil.getScript("limit.lua");
        this.key = key;
        this.maxPermits = maxPermits;
        this.intervalMilliseconds = intervalMilliseconds;
    }

    private Jedis getJedis() throws Exception {
        return jedisPool.borrowObject();
    }

    private void returnJedis(Jedis redis) {
        jedisPool.returnObject(redis);
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
