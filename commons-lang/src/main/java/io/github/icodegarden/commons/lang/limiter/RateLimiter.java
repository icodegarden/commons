package io.github.icodegarden.commons.lang.limiter;

/**
 * 计数、漏桶、令牌桶、滑动窗口、三色速率标记
 * @author Fangfang.Xu
 *
 */
public interface RateLimiter {

	default String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 请求的重量1
	 * 
	 * @return
	 */
	default boolean isAllowable() {
		return isAllowable(1);
	}

	/**
	 * 
	 * @param weight 请求的重量
	 * @return
	 */
	boolean isAllowable(int weight);
}
