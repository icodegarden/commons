package io.github.icodegarden.commons.lang.limiter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 在给定的时间间隔进行计数，计数间隔中允许突发，如果计数间隔开始突发导致计数满了，则直到间隔结束资源将会进入闲置状态
 * 
 * @author Fangfang.Xu
 *
 */
public class CounterRateLimiter extends CounterRateLimiterSupport {

	private int count;

	private AtomicInteger token;

	/**
	 * 
	 * @param count    在给定的interval中允许的次数
	 * @param interval 计数间隔millis
	 */
	public CounterRateLimiter(int count, long interval) {
		super(interval);
		if (count <= 0) {
			throw new IllegalArgumentException("count must gt 0");
		}
		this.count = count;
	}

	@Override
	protected void resetToken() {
		this.token = new AtomicInteger(count);
	}

	@Override
	protected int getAndDecrement(int value) {
		return token.getAndAdd(-value);
	}
}