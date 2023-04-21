package io.github.icodegarden.commons.redis.limiter;

import io.github.icodegarden.commons.redis.JedisClusterRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisClusterRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisClusterRedisCounterRateLimiterTests extends RedisCounterRateLimiterTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisClusterRedisExecutor(JedisClusterRedisExecutorTests.newJedisCluster());
	}
}
