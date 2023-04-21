package io.github.icodegarden.commons.redis.limiter;

import io.github.icodegarden.commons.redis.JedisPoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisPoolRedisCounterRateLimiterTests extends RedisCounterRateLimiterTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
	}
}
