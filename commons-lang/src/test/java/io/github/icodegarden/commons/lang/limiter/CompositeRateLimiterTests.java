package io.github.icodegarden.commons.lang.limiter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CompositeRateLimiterTests {
	
	@Test
	public void isAllowable() throws Exception {
		DefaultRateLimiter rateLimiter1 = new DefaultRateLimiter("name1", 3, 1000);
		DefaultRateLimiter rateLimiter2 = new DefaultRateLimiter("name2", 2, 1000);

		CompositeRateLimiter compositeRateLimiter = new CompositeRateLimiter(Arrays.asList(rateLimiter1,rateLimiter2));
		
		assertEquals(true, rateLimiter1.isAllowable());//先消耗1次
		assertEquals(true, rateLimiter1.isAllowable());//先消耗1次
		assertEquals(true, compositeRateLimiter.isAllowable());
		assertEquals(false, compositeRateLimiter.isAllowable());//rateLimiter1此时已经没有次数了

		Thread.sleep(1100);
		assertEquals(true, compositeRateLimiter.isAllowable());
		assertEquals(true, compositeRateLimiter.isAllowable());
		assertEquals(false, compositeRateLimiter.isAllowable());//rateLimiter2没有次数了
	}
}
