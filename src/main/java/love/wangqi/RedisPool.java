package love.wangqi;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午10:05
 */
public abstract class RedisPool {
    private JedisPool jedisPool = null;
    private String host = "localhost";
    private int port = 6379;
    private String password = null;

    public RedisPool() {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            jedisPool = new JedisPool(config, host, port, 3000, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Jedis getJedis() throws Exception {
        return jedisPool.getResource();
    }

    protected void closeJedis(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
