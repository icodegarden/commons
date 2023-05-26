package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.redis.JedisPoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisPoolRedisReentrantReadWriteLockTests extends RedisReentrantReadWriteLockTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
	}
}
