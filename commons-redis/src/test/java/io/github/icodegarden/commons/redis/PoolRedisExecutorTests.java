package io.github.icodegarden.commons.redis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import redis.clients.jedis.JedisPool;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class PoolRedisExecutorTests extends RedisExecutorTests {

	@Override
	protected RedisExecutor newInstance() {
		return new PoolRedisExecutor(newJedisPool());
	}

	public static JedisPool newJedisPool() {
		return new JedisPool(new GenericObjectPoolConfig(), "172.22.122.23", 6399, 10000, null);
	}
}
