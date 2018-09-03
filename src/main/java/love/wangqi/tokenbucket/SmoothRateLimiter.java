package love.wangqi.tokenbucket;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午7:34
 */
public abstract class SmoothRateLimiter extends RateLimiter {

    String key;

    String script;

    double storedPermits = 0;

    double permitsPerSecond = 1;

    double maxPermits = 0;

    double stableIntervalMicros = 0;


    protected SmoothRateLimiter(SleepingStopwatch stopwatch) {
        super(stopwatch);
    }

    @Override
    void doSetRate(double permitsPerSecond) {
        queryWaitMicros(0, null);
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
    long queryWaitMicros(int permits, Long timeoutMicros) {
        List<String> keys = Arrays.asList(key, String.valueOf(maxPermits), String.valueOf(permitsPerSecond));
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(permits));
        if (timeoutMicros != null) {
            args.add(String.valueOf(timeoutMicros));
        }
        try {
            Jedis redis = getJedis();
            Object result = redis.eval(script, keys, args);
            return (long) result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
