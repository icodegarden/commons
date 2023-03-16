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

	private final byte[] key;
	
//	使用redis hash的原子操作，该hash key及其field不需要提前建
//	private final byte[] f_key;

	private RedisExecutor redisExecutor;

	public RedisSequenceManager(String moduleName, RedisExecutor redisExecutor) {
		this(moduleName, redisExecutor, 100);
	}

	public RedisSequenceManager(String moduleName, RedisExecutor redisExecutor, long increment) {
		super(moduleName);
		Assert.notNull(redisExecutor, "redisExecutor must not null");
		this.redisExecutor = redisExecutor;

		this.increment = increment;

		this.key = ("redis:sequence:" + moduleName).getBytes(Charset.forName("utf-8"));

//		this.f_key = moduleName.getBytes(Charset.forName("utf-8"));
	}

	@Override
	public long getIncrement() {
		return increment;
	}

//	public void setKey(byte[] key) {
//		this.key = key;
//	}

	@Override
	public long nextMaxId() {
//		return redisExecutor.hincrBy(key, f_key, increment);
		return redisExecutor.incrBy(key, increment);
	}
}
