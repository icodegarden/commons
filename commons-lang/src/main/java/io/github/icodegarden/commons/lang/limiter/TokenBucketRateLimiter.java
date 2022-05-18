package io.github.icodegarden.commons.lang.limiter;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 令牌桶算法，允许一定的突发流量，固定进入令牌，任务速率取令牌访问<br>
 * 桶满则丢弃，按给定的速率放令牌<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class TokenBucketRateLimiter implements RateLimiter {
	private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimiter.class);

	private int tokenSize;
	private int inputSize;
	private Duration inputDuration;

	private long lastResetTime = System.currentTimeMillis();

	private AtomicInteger token;

	/**
	 * 
	 * @param tokenSize       令牌数量
	 * @param inputSize       放入大小
	 * @param outflowDuration 放入时间单位
	 */
	public TokenBucketRateLimiter(int tokenSize, int inputSize, Duration inputDuration) {
		this.token = new AtomicInteger(tokenSize);

		this.tokenSize = tokenSize;
		this.inputSize = inputSize;
		this.inputDuration = inputDuration;
	}

	private void reset() {
		long now = System.currentTimeMillis();

		long cost = now - lastResetTime;
		double rate = cost * 1.0 / inputDuration.toMillis();
		int shouldInput = (int) (inputSize * rate);
		if (shouldInput == 0) {
			return;
		}

		token.updateAndGet(pre -> {
			int v = pre + shouldInput;
			return v <= tokenSize ? v : tokenSize;
		});
		this.lastResetTime = System.currentTimeMillis();
	}

	@Override
	public boolean isAllowable(int weight) {
		if (token.intValue() < weight) {
			/**
			 * 当桶不够了再放入即可
			 */
			reset();
		}
		if (token.intValue() < weight) {
			if (log.isInfoEnabled()) {
				log.info("{}:{} not allowed", TokenBucketRateLimiter.class.getSimpleName(), getName());
			}
			return false;
		}
		token.addAndGet(-weight);
		return true;
	}
}