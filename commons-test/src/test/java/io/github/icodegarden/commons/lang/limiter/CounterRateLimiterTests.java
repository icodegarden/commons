package io.github.icodegarden.commons.lang.limiter;

import io.github.icodegarden.commons.test.limiter.AbstractCounterRateLimiterTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CounterRateLimiterTests extends AbstractCounterRateLimiterTests {

	@Override
	protected RateLimiter newCounterRateLimiter(int count, long interval) {
		return new CounterRateLimiter(4, 1000);
	}
}
