package love.wangqi;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.reflect.Method;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/9/3 上午10:57
 */
public class JedisPoolProxy implements MethodInterceptor {
    private JedisPool jedisPool;

    private static final Logger log = LoggerFactory.getLogger(JedisPoolProxy.class);

    public JedisPoolProxy(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        try (Jedis jedis = jedisPool.getResource()) {
            return method.invoke(jedis, args);
        }
    }
}
