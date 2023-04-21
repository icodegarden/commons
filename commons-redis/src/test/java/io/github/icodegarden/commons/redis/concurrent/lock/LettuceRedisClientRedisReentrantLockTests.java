package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.redis.LettuceRedisClientRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.lettuce.LettuceRedisClientRedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LettuceRedisClientRedisReentrantLockTests extends RedisReentrantLockTests {

	/**
	 * lettuce的客户端是基于netty，一般只需要1个client即可，建多了会占很多资源导致selector打不开
	 */
	private static RedisExecutor redisExecutor = new LettuceRedisClientRedisExecutor(LettuceRedisClientRedisExecutorTests.newRedisClient());
	
	protected RedisExecutor newRedisExecutor() {
		return redisExecutor;
	}
}
