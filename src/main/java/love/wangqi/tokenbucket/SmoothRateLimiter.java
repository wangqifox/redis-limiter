package love.wangqi.tokenbucket;

import love.wangqi.ScriptUtil;
import redis.clients.jedis.Jedis;

import java.util.Arrays;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午7:34
 */
public abstract class SmoothRateLimiter extends RateLimiter {

    static final class SmoothBursty extends SmoothRateLimiter {
        final double maxBurstSeconds;

        SmoothBursty(String key, SleepingStopwatch stopwatch, double maxBurstSeconds) {
            super(stopwatch);
            this.script = ScriptUtil.getScript("smooth_ratelimiter.lua");
            this.key = key;
            this.maxBurstSeconds = maxBurstSeconds;
        }

        @Override
        void doSetRate(double permitsPerSecond, double stableIntervalMicros) {
            double oldMaxPermits = this.maxPermits;
            this.permitsPerSecond = permitsPerSecond;
            maxPermits = maxBurstSeconds * permitsPerSecond;
            if (oldMaxPermits == Double.POSITIVE_INFINITY) {
                storedPermits = maxPermits;
            } else {
                storedPermits =
                        (oldMaxPermits == 0.0)
                        ? 0.0 : storedPermits * maxPermits / oldMaxPermits;
            }
        }
    }

    String key;

    String script;

    double storedPermits;

    double permitsPerSecond;

    double maxPermits;

    double stableIntervalMicros;


    private SmoothRateLimiter(SleepingStopwatch stopwatch) {
        super(stopwatch);
    }

    @Override
    void doSetRate(double permitsPerSecond) {
        double stableIntervalMicros = SECONDS.toMicros(1L) / permitsPerSecond;
        this.stableIntervalMicros = stableIntervalMicros;
        doSetRate(permitsPerSecond, stableIntervalMicros);
    }

    abstract void doSetRate(double permitsPerSecond, double stableIntervalMicros);

    @Override
    double doGetRate() {
        return SECONDS.toMicros(1L) / stableIntervalMicros;
    }

    @Override
    long queryEarliestAvailable(int permits, long timeoutMicros) {
        Jedis redis = null;
        try {
            redis = getJedis();
            Object result = redis.eval(script,
                    Arrays.asList(key, String.valueOf(maxPermits), String.valueOf(permitsPerSecond)),
                    Arrays.asList(String.valueOf(permits), String.valueOf(timeoutMicros)));
            return (long) result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeJedis(redis);
        }
        return 0;
    }

    @Override
    final long reserveEarliestAvailable(int requiredPermits) {
        Jedis redis = null;
        try {
            redis = getJedis();
            Object result = redis.eval(script,
                    Arrays.asList(key, String.valueOf(maxPermits), String.valueOf(permitsPerSecond)),
                    Arrays.asList(String.valueOf(requiredPermits)));
            return (long) result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeJedis(redis);
        }
        return 0;
    }
}
