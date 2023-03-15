package io.github.icodegarden.commons.redis.sequence;

import io.github.icodegarden.commons.lang.sequence.SequenceManager;
import io.github.icodegarden.commons.lang.sequence.SequenceManagerTests;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class RedisSequenceManagerTests extends SequenceManagerTests {

	@Override
	protected SequenceManager getForOneProcess() {
		return newSequenceManager();
	}

	@Override
	protected SequenceManager newForMultiProcess() {
		return newSequenceManager();
	}

	private SequenceManager newSequenceManager() {
		RedisExecutor redisExecutor = new PoolRedisExecutor(PoolRedisExecutorTests.newJedisPool());
		return new RedisSequenceManager("GLOBAL", redisExecutor, 100);
	}

}
