package io.github.icodegarden.commons.lang.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CounterRateLimiterTests {

	@Test
	public void isAllowable() throws Exception {
		CounterRateLimiter rateLimiter = new CounterRateLimiter(4, 1000);

		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(false, rateLimiter.isAllowable(2));

		Thread.sleep(1100);
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(false, rateLimiter.isAllowable(2));
	}
}
