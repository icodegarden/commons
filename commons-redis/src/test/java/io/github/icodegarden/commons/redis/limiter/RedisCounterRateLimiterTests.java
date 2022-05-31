package io.github.icodegarden.commons.redis.limiter;

import io.github.icodegarden.commons.lang.limiter.RateLimiter;
import io.github.icodegarden.commons.redis.PoolRedisExecutor;
import io.github.icodegarden.commons.redis.PoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.test.limiter.AbstractCounterRateLimiterTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisCounterRateLimiterTests extends AbstractCounterRateLimiterTests {

	RedisExecutor redisExecutor = new PoolRedisExecutor(PoolRedisExecutorTests.newJedisPool());

	@Override
	protected RateLimiter newCounterRateLimiter(int count, long interval) {
		return new RedisCounterRateLimiter(redisExecutor, "RedisCounterRateLimiter", count, interval);
	}

}
