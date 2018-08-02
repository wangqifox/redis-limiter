package love.wangqi.tokenbucket;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import love.wangqi.RedisPool;
import love.wangqi.tokenbucket.SmoothRateLimiter.SmoothBursty;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午7:43
 */
public abstract class RateLimiter extends RedisPool {

    public static RateLimiter create(String key, double permitsPerSecond) {
        return create(key, permitsPerSecond, SleepingStopwatch.createFromSystemTimer());
    }

    static RateLimiter create(String key, double permitsPerSecond, SleepingStopwatch stopwatch) {
        RateLimiter rateLimiter = new SmoothBursty(key, stopwatch, 1.0);
        rateLimiter.setRate(permitsPerSecond);
        return rateLimiter;
    }

    private final SleepingStopwatch stopwatch;

    private volatile Object mutexDoNotUseDirectly;

    private Object mutex() {
        Object mutex = mutexDoNotUseDirectly;
        if (mutex == null) {
            synchronized (this) {
                mutex = mutexDoNotUseDirectly;
                if (mutex == null) {
                    mutexDoNotUseDirectly = mutex = new Object();
                }
            }
        }
        return mutex;
    }

    RateLimiter(SleepingStopwatch stopwatch) {
        this.stopwatch = checkNotNull(stopwatch);
    }

    public final void setRate(double permitsPerSecond) {
        checkArgument(permitsPerSecond > 0.0 && !Double.isNaN(permitsPerSecond), "rate must be positive");
        synchronized (mutex()) {
            doSetRate(permitsPerSecond);
        }
    }

    abstract void doSetRate(double permitsPerSecond);

    public final double getRate() {
        synchronized (mutex()) {
            return doGetRate();
        }
    }

    abstract double doGetRate();

    public double acquire() {
        return acquire(1);
    }

    public double acquire(int permits) {
        long microToWait = reserve(permits);
        stopwatch.sleepMicrosUninterruptibly(microToWait);
        return 1.0 * microToWait / SECONDS.toMicros(1L);
    }

    final long reserve(int permits) {
        checkPermits(permits);
        synchronized (mutex()) {
            return reserveAndGetWaitLength(permits);
        }
    }

    public boolean tryAcquire(long timeout, TimeUnit unit) {
        return tryAcquire(1, timeout, unit);
    }

    public boolean tryAcquire(int permits) {
        return tryAcquire(permits, 0, MICROSECONDS);
    }

    public boolean tryAcquire() {
        return tryAcquire(1, 0, MICROSECONDS);
    }

    public boolean tryAcquire(int permits, long timeout, TimeUnit unit) {
        long timeoutMicros = max(unit.toMicros(timeout), 0);
        checkPermits(permits);
        long microsToWait;
        synchronized (mutex()) {
            if (!canAcquire(permits, timeoutMicros)) {
                return false;
            } else {
                microsToWait = reserveAndGetWaitLength(permits);
            }
        }
        stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }

    private boolean canAcquire(int permits, long timeoutMicros) {
        return queryEarliestAvailable(permits, timeoutMicros) <= timeoutMicros;
    }

    final long reserveAndGetWaitLength(int permits) {
        long momentAvailable = reserveEarliestAvailable(permits);
        return max(momentAvailable, 0);
    }

    abstract long queryEarliestAvailable(int permits, long timeoutMicros);

    abstract long reserveEarliestAvailable(int permits);

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "RateLimiter[stableRate=%3.1fqps]", getRate());
    }

    abstract static class SleepingStopwatch {
        protected  SleepingStopwatch() {}

        protected abstract long readMicros();

        protected abstract void sleepMicrosUninterruptibly(long micros);

        public static SleepingStopwatch createFromSystemTimer() {
            return new SleepingStopwatch() {
                final Stopwatch stopwatch = Stopwatch.createStarted();

                @Override
                protected long readMicros() {
                    return stopwatch.elapsed(MICROSECONDS);
                }

                @Override
                protected void sleepMicrosUninterruptibly(long micros) {
                    if (micros > 0) {
                        Uninterruptibles.sleepUninterruptibly(micros, MICROSECONDS);
                    }
                }
            };
        }
    }

    private static void checkPermits(int permits) {
        checkArgument(permits > 0, "Requested permits (%s) must be positive", permits);
    }
}
