package io.github.icodegarden.commons.redis.sequence;

import java.nio.charset.Charset;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.sequence.AtomicSequenceManager;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisSequenceManager extends AtomicSequenceManager {

	private final long increment;

	private byte[] key = "redis:sequence:manager".getBytes(Charset.forName("utf-8"));
	private final byte[] f_key;

	private RedisExecutor redisExecutor;

	public RedisSequenceManager(String moduleName, RedisExecutor redisExecutor, long increment) {
		super(moduleName);
		Assert.notNull(redisExecutor, "redisExecutor must not null");
		this.redisExecutor = redisExecutor;

		this.increment = increment;

		this.f_key = moduleName.getBytes(Charset.forName("utf-8"));
	}

	@Override
	public long getIncrement() {
		return increment;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	@Override
	public long nextMaxId() {
		return redisExecutor.hincrBy(key, f_key, increment);
	}
}
