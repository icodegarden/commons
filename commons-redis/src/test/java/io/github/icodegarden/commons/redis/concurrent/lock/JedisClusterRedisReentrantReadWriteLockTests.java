package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.redis.JedisClusterRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisClusterRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisClusterRedisReentrantReadWriteLockTests extends RedisReentrantReadWriteLockTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisClusterRedisExecutor(JedisClusterRedisExecutorTests.newJedisCluster());
	}
}
