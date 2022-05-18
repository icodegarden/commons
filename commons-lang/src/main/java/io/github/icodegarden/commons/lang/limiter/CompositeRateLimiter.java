package io.github.icodegarden.commons.lang.limiter;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class CompositeRateLimiter implements RateLimiter {

	private final List<RateLimiter> rateLimiters;

	public CompositeRateLimiter(List<RateLimiter> rateLimiters) {
		this.rateLimiters = rateLimiters;
	}

	@Override
	public boolean isAllowable() {
		return rateLimiters.stream().allMatch(RateLimiter::isAllowable);
	}
}
