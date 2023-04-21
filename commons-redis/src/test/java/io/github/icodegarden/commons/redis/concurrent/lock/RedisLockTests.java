package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class RedisLockTests extends DistributedLockTests {

	@Override
	protected DistributedLock newDistributedLock(String name) {
		RedisExecutor redisExecutor = newRedisExecutor();
		return new RedisLock(redisExecutor, name, getExpireSeconds());
	}

	@Override
	protected long getExpireSeconds() {
		return 3;
	}
	
	protected abstract RedisExecutor newRedisExecutor();
	
}
