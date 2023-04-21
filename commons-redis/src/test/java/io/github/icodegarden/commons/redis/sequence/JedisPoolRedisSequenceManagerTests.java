package io.github.icodegarden.commons.redis.sequence;

import io.github.icodegarden.commons.redis.JedisPoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisPoolRedisSequenceManagerTests extends RedisSequenceManagerTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
	}
}
