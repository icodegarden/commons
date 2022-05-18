package io.github.icodegarden.commons.lang.limiter;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 在给定的时间间隔进行计数，计数间隔中允许突发，如果计数间隔开始突发导致计数满了，则直到间隔结束资源将会进入闲置状态
 * @author Fangfang.Xu
 *
 */
public class CounterRateLimiter implements RateLimiter {
	private static final Logger log = LoggerFactory.getLogger(CounterRateLimiter.class);

	private long lastResetTime;

	private long interval;

	private AtomicInteger token;

	private int count;

	/**
	 * 
	 * @param name
	 * @param count    在给定的interval中允许的次数
	 * @param interval 计数间隔millis
	 */
	public CounterRateLimiter(int count, long interval) {
		if (count <= 0) {
			throw new IllegalArgumentException("count must gt 0");
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must gt 0");
		}
		this.count = count;
		this.interval = interval;

		start();
	}

	private CounterRateLimiter start() {
		this.lastResetTime = System.currentTimeMillis();
		this.token = new AtomicInteger(count);
		return this;
	}

	@Override
	public boolean isAllowable(int weight) {
		long now = System.currentTimeMillis();
		/**
		 * 刷新计数
		 */
		if (now > lastResetTime + interval) {
			token = new AtomicInteger(count);
			lastResetTime = now;
		}
		if (token.intValue() < weight) {
			if (log.isInfoEnabled()) {
				log.info("{}:{} not allowed", CounterRateLimiter.class.getSimpleName(), getName());
			}
			return false;
		}
		token.addAndGet(-weight);
		return true;
	}
}