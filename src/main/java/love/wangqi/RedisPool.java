package love.wangqi;

import net.sf.cglib.proxy.Enhancer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/2 上午10:05
 */
public abstract class RedisPool {
    private static JedisPool jedisPool = null;
    private static String host = "localhost";
    private static int port = 6379;
    private static String password = null;

    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            jedisPool = new JedisPool(config, host, port, 3000, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Jedis getJedis() {
        JedisPoolProxy proxy = new JedisPoolProxy(jedisPool);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(Jedis.class);
        enhancer.setCallback(proxy);
        return (Jedis) enhancer.create();
    }
}
