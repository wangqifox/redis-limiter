package love.wangqi;

import org.apache.commons.pool2.impl.GenericObjectPool;
import redis.clients.jedis.Jedis;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午10:05
 */
public abstract class RedisPool {
    private GenericObjectPool<Jedis> jedisPool;

    public RedisPool() {
        jedisPool = new GenericObjectPool<>(new JedisPooledFactory("localhost", 6379));
    }

    protected Jedis getJedis() throws Exception {
        return jedisPool.borrowObject();
    }

    protected void returnJedis(Jedis redis) {
        if (redis != null) {
            jedisPool.returnObject(redis);
        }
    }
}
