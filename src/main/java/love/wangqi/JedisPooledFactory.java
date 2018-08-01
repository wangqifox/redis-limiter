package love.wangqi;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import redis.clients.jedis.Jedis;

/**
 * @author: wangqi
 * @description:
 * @date: Created in 2018/8/1 下午7:53
 */
public class JedisPooledFactory extends BasePooledObjectFactory<Jedis> {
    private String url;
    private int port;

    public JedisPooledFactory(String url, int port) {
        this.url = url;
        this.port = port;
    }

    @Override
    public Jedis create() throws Exception {
        return new Jedis(url, port);
    }

    @Override
    public boolean validateObject(PooledObject<Jedis> p) {
        if (!p.getObject().isConnected()) {
            return false;
        }
        return super.validateObject(p);
    }

    @Override
    public void destroyObject(PooledObject<Jedis> p) throws Exception {
        p.getObject().close();
        super.destroyObject(p);
    }

    @Override
    public PooledObject<Jedis> wrap(Jedis obj) {
        return new DefaultPooledObject<>(obj);
    }
}
