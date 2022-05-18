package io.github.icodegarden.commons.lang.limiter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RateLimiter {

	/**
	 * @return
	 */
	boolean isAllowable();
}
