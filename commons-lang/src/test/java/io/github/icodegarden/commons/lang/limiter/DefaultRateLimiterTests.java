package io.github.icodegarden.commons.lang.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultRateLimiterTests {

	@Test
	public void isAllowable() throws Exception {
		DefaultRateLimiter rateLimiter = new DefaultRateLimiter("name", 2, 1000);

		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(false, rateLimiter.isAllowable());

		Thread.sleep(1100);
		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(true, rateLimiter.isAllowable());
		assertEquals(false, rateLimiter.isAllowable());
	}
}
