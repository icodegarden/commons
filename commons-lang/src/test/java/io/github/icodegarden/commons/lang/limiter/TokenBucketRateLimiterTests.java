package io.github.icodegarden.commons.lang.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class TokenBucketRateLimiterTests {

	@Test
	public void isAllowable() throws Exception {
		TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(4, 2, Duration.ofSeconds(1));

		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(true, rateLimiter.isAllowable(2));
		assertEquals(false, rateLimiter.isAllowable(2));// 还未流出

		Thread.sleep(1000);
		assertEquals(true, rateLimiter.isAllowable(2));// 流出1个
		assertEquals(false, rateLimiter.isAllowable(2));// 未流出
	}
}
