package love.wangqi.tokenbucket;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.max;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.Uninterruptibles;
import redis.clients.jedis.Jedis;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午7:43
 */
public abstract class RateLimiter {
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
        checkPermits(permits);
        long microToWait = waitMicros(permits);
        stopwatch.sleepMicrosUninterruptibly(microToWait);
        return 1.0 * microToWait / SECONDS.toMicros(1L);
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
        long microsToWait = queryWaitMicros(permits, timeoutMicros);
        if (microsToWait > timeoutMicros) {
            return false;
        }
        stopwatch.sleepMicrosUninterruptibly(microsToWait);
        return true;
    }

    final long waitMicros(int permits) {
        long waitMicros = queryWaitMicros(permits, null);
        return max(waitMicros, 0);
    }

    abstract long queryWaitMicros(int permits, Long timeoutMicros);

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

    protected abstract Jedis getJedis();
}
