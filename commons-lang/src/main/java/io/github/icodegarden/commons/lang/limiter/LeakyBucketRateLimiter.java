package io.github.icodegarden.commons.lang.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 漏桶算法，以恒定的速率访问<br>
 * 桶满则丢弃，按给定的速率流出水<br>
 * 
 * 目前没有实现固定速率出水访问server，效果上与令牌桶等同，但思想不同
 * 
 * @author Fangfang.Xu
 *
 */
public class LeakyBucketRateLimiter implements RateLimiter {
	private static final Logger log = LoggerFactory.getLogger(LeakyBucketRateLimiter.class);

	private int bucketSize;
	private int outflowSize;
	private Duration outflowDuration;

	private long lastResetTime = System.currentTimeMillis();

	private AtomicInteger token;

	/**
	 * 
	 * @param bucketSize      桶大小
	 * @param outflowSize     流出大小
	 * @param outflowDuration 流出时间单位
	 */
	public LeakyBucketRateLimiter(int bucketSize, int outflowSize, Duration outflowDuration) {
		this.token = new AtomicInteger(bucketSize);

		this.bucketSize = bucketSize;
		this.outflowSize = outflowSize;
		this.outflowDuration = outflowDuration;
	}

	private void reset() {
		long now = System.currentTimeMillis();

		long cost = now - lastResetTime;
		double rate = cost * 1.0 / outflowDuration.toMillis();
		int shouldOutflow = (int) (outflowSize * rate);
		System.out.println("shouldOutflow=" + shouldOutflow);
		if (shouldOutflow == 0) {
			return;
		}

		token.updateAndGet(pre -> {
			int v = pre + shouldOutflow;
			return v <= bucketSize ? v : bucketSize;
		});
		this.lastResetTime = System.currentTimeMillis();
	}

	@Override
	public boolean isAllowable(int weight) {
		if (token.intValue() < weight) {
			/**
			 * 当桶满了再放水即可
			 */
			reset();
		}
		if (token.intValue() < weight) {
			if (log.isInfoEnabled()) {
				log.info("{}:{} not allowed", LeakyBucketRateLimiter.class.getSimpleName(), getName());
			}
			return false;
		}
		token.addAndGet(-weight);
		return true;
	}
}