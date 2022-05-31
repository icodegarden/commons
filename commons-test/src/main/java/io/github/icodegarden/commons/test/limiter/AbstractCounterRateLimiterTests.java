package io.github.icodegarden.commons.test.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.limiter.RateLimiter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractCounterRateLimiterTests {

	protected abstract RateLimiter newCounterRateLimiter(int count, long interval);

	@Test
	public void isAllowable() throws Exception {
		RateLimiter rateLimiter = newCounterRateLimiter(4, 1000);

		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(false, rateLimiter.isAllowable(2));

		Thread.sleep(1100);
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(false, rateLimiter.isAllowable(2));
	}
}
