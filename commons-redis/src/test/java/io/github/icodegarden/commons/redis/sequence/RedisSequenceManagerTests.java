package io.github.icodegarden.commons.redis.sequence;

import io.github.icodegarden.commons.lang.sequence.SequenceManager;
import io.github.icodegarden.commons.lang.sequence.SequenceManagerTests;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class RedisSequenceManagerTests extends SequenceManagerTests {

	@Override
	protected SequenceManager getForOneProcess() {
		return newSequenceManager();
	}

	@Override
	protected SequenceManager newForMultiProcess() {
		return newSequenceManager();
	}

	private SequenceManager newSequenceManager() {
		return new RedisSequenceManager("GLOBAL", newRedisExecutor(), 100);
	}

	protected abstract RedisExecutor newRedisExecutor();
}
