package io.github.icodegarden.commons.redis.limiter;

import java.nio.charset.Charset;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.limiter.CounterRateLimiterSupport;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 作用参考 {@link io.github.icodegarden.commons.lang.limiter.CounterRateLimiter}
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisCounterRateLimiter extends CounterRateLimiterSupport {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final byte[] SETBATCH_SCRIPT = "local v = redis.call('get',KEYS[1]);redis.call('decrBy',KEYS[1],ARGV[1]);return v;"
			.getBytes(Charset.forName("utf-8"));

	private byte[] count;

	private byte[] key;

	private RedisExecutor redisExecutor;

	/**
	 * 
	 * @param key      redis计数的key
	 * @param count    在给定的interval中允许的次数
	 * @param interval 计数间隔millis
	 */
	public RedisCounterRateLimiter(RedisExecutor redisExecutor, String key, int count, long interval) {
		super(interval);

		Assert.notNull(redisExecutor, "redisExecutor must not null");
		Assert.hasText(key, "key must not empty");
		if (count <= 0) {
			throw new IllegalArgumentException("count must gt 0");
		}
		if (interval <= 0) {
			throw new IllegalArgumentException("interval must gt 0");
		}
		this.redisExecutor = redisExecutor;
		this.key = key.getBytes(CHARSET);
		this.count = Integer.valueOf(count).toString().getBytes(CHARSET);
	}

	@Override
	protected void resetToken() {
		redisExecutor.set(key, count);
	}

	@Override
	protected int getAndDecrement(int value) {
		/**
		 * 直接使用decrBy得到的是减去后的结果，相当于decrementAndGet
		 */
		
		byte[] valuebs = Integer.valueOf(value).toString().getBytes(CHARSET);
		byte[] bs = (byte[]) redisExecutor.eval(SETBATCH_SCRIPT, 1, key, valuebs);

		String str = new String(bs, CHARSET);
		int v = Integer.parseInt(str);
		return v;
	}
}