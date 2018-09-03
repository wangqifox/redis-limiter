package love.wangqi.tokenbucket;

import love.wangqi.ScriptUtil;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/3 上午10:40
 */
public abstract class SmoothBursty extends SmoothRateLimiter {
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