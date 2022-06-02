package io.github.icodegarden.commons.lang.limiter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CounterRateLimiterSupport implements RateLimiter {
	private static final Logger log = LoggerFactory.getLogger(CounterRateLimiterSupport.class);

	private long interval;

	private volatile long lastResetTime;

	/**
	 * @param interval 计数间隔millis
	 */
	public CounterRateLimiterSupport(long interval) {
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must gt 0");
		}
		this.interval = interval;
	}

	@Override
	public boolean isAllowable(int weight) {
		long now = System.currentTimeMillis();
		/**
		 * 刷新计数
		 */
		if (now > lastResetTime + interval) {// 首次lastResetTime=0,会进来
			synchronized (this) {
				if (now > lastResetTime + interval) {
					resetToken();
					lastResetTime = now;
				}
			}
		}

		if (getAndDecrement(weight) < weight) {
			if (log.isInfoEnabled()) {
				log.info("{}:{} not allowed", CounterRateLimiterSupport.class.getSimpleName(), getName());
			}
			return false;
		}

		return true;
	}

	protected abstract void resetToken();

	protected abstract int getAndDecrement(int value);

}