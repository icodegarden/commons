package io.github.icodegarden.commons.redis.limiter;

import io.github.icodegarden.commons.lang.limiter.RateLimiter;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.test.limiter.AbstractCounterRateLimiterTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class RedisCounterRateLimiterTests extends AbstractCounterRateLimiterTests {

	@Override
	protected RateLimiter newCounterRateLimiter(int count, long interval) {
		return new RedisCounterRateLimiter(newRedisExecutor(), "RedisCounterRateLimiter", count, interval);
	}

	protected abstract RedisExecutor newRedisExecutor();
}
