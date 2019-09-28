package redis;

import com.mihone.redismq.redis.RedisUtils;
import org.junit.Test;
import redis.clients.jedis.Jedis;

public class RedisTest {
    @Test
    public void redisTest(){
        Jedis jedis = RedisUtils.getJedis();
        jedis.set("k2","v2");
        RedisUtils.returnJedis(jedis);
    }
}
