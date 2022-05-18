package io.github.icodegarden.commons.lang.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LeakyBucketRateLimiterTests {

	@Test
	public void isAllowable() throws Exception {
		LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter();

		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(false, rateLimiter.isAllowable());//还未流出

		Thread.sleep(100);
		assertEquals(true, rateLimiter.isAllowable());//流出
		assertEquals(false, rateLimiter.isAllowable());//未流出
	}
}
