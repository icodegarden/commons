package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.RedisTemplateRedisExecutorTests;
import io.github.icodegarden.commons.redis.spring.RedisTemplateRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisTemplateRedisLockTests extends RedisLockTests {

	/**
	 * lettuce的客户端是基于netty，一般只需要1个client即可，建多了会占很多资源导致selector打不开
	 */
	private static RedisExecutor redisExecutor = new RedisTemplateRedisExecutor(RedisTemplateRedisExecutorTests.newRedisTemplate());
	
	protected RedisExecutor newRedisExecutor() {
		return redisExecutor;
	}
}