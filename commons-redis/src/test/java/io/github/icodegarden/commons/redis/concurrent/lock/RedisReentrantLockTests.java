package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class RedisReentrantLockTests extends DistributedReentrantLockTests {

	@Override
	protected DistributedReentrantLock newLock(String name) {
		return new RedisReentrantLock(newRedisExecutor(), name, 5L);
	}

	protected abstract RedisExecutor newRedisExecutor();
}
