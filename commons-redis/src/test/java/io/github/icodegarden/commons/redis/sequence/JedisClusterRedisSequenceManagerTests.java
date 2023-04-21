package io.github.icodegarden.commons.redis.sequence;

import io.github.icodegarden.commons.redis.JedisClusterRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisClusterRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisClusterRedisSequenceManagerTests extends RedisSequenceManagerTests {

	protected RedisExecutor newRedisExecutor() {
		return new JedisClusterRedisExecutor(JedisClusterRedisExecutorTests.newJedisCluster());
	}
}
