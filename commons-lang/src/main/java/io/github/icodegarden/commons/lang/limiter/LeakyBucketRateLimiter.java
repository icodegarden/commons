package io.github.icodegarden.commons.lang.limiter;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LeakyBucketRateLimiter implements RateLimiter {
	private static final Logger log = LoggerFactory.getLogger(LeakyBucketRateLimiter.class);

	private String name;

	private long lastResetTime;

	private long interval;

	private AtomicInteger token;

	private int rate;

	/**
	 * 
	 * @param name
	 * @param rate     在给定的interval中允许的次数
	 * @param interval 刷新间隔millis
	 */
	public LeakyBucketRateLimiter(String name, int bucketSize, int outflowSize,Duration outflowDuration) {
		if (name == null) {
			throw new IllegalArgumentException("name must not null");
		}
		if (rate <= 0) {
			throw new IllegalArgumentException("rate must gt 0");
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must gt 0");
		}
		this.name = name;
		this.rate = rate;
		this.interval = interval;
		
		long millis = outflowDuration.toMillis();
		long millis10 = millis/10;
		
		int size = (int)(outflowSize/(millis/10));

		start();
	}

	private LeakyBucketRateLimiter start() {
		this.lastResetTime = System.currentTimeMillis();
		this.token = new AtomicInteger(rate);
		return this;
	}

	public String getName() {
		return name;
	}

	public long getLastResetTime() {
		return lastResetTime;
	}

	public long getInterval() {
		return interval;
	}

	public int getRate() {
		return rate;
	}

	@Override
	public boolean isAllowable() {
		long now = System.currentTimeMillis();
		if (now > lastResetTime + interval) {
			token = new AtomicInteger(rate);
			lastResetTime = now;
		}
		if (token.intValue() < 1) {
			if (log.isInfoEnabled()) {
				log.info("rate limit:{} not allowed", name);
			}
			return false;
		}
		token.decrementAndGet();
		return true;
	}
}