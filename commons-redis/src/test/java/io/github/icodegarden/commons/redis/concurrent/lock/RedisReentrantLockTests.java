package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantLockTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisReentrantLockTests extends DistributedReentrantLockTests {

	RedisExecutor redisExecutor = new PoolRedisExecutor(PoolRedisExecutorTests.newJedisPool());

	@Override
	protected DistributedReentrantLock newLock(String name) {
		return new RedisReentrantLock(redisExecutor, name, 5L);
	}

}
