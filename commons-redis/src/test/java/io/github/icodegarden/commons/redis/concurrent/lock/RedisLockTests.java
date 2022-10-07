package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.TemplateRedisExecutor;
import io.github.icodegarden.commons.redis.TemplateRedisExecutorTests;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisLockTests extends DistributedLockTests {

	/**
	 * 以下都可
	 */
	RedisExecutor redisExecutor = new TemplateRedisExecutor(TemplateRedisExecutorTests.newRedisTemplate());
//	RedisExecutor redisExecutor = new PoolRedisExecutor(PoolRedisExecutorTests.newJedisPool());

	@Override
	protected DistributedLock newDistributedLock(String name) {
		return new RedisLock(redisExecutor, name, 30L);
	}

}
