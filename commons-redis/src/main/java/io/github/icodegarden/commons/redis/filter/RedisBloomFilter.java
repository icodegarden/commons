package io.github.icodegarden.commons.redis.filter;

import java.nio.charset.StandardCharsets;

import io.github.icodegarden.commons.lang.algorithm.HashFunction;
import io.github.icodegarden.commons.lang.filter.AbstractBloomFilter;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisBloomFilter extends AbstractBloomFilter {

	private final byte[] bitMapKey;

	private final RedisExecutor redisExecutor;

	/**
	 * 默认countOfHasher=3
	 */
	public RedisBloomFilter(String bitMapKey, RedisExecutor redisExecutor) {
		this(bitMapKey, redisExecutor, 3);
	}

	/**
	 * 使用java string的hash算法
	 * 
	 * @param bitSize
	 * @param countOfHasher
	 * @param shouldFilter
	 */
	public RedisBloomFilter(String bitMapKey, RedisExecutor redisExecutor, int countOfHasher) {
		super(countOfHasher);

		this.bitMapKey = bitMapKey.getBytes(StandardCharsets.UTF_8);
		this.redisExecutor = redisExecutor;
	}

	public RedisBloomFilter(String bitMapKey, RedisExecutor redisExecutor, HashFunction[] hashers) {
		super(hashers);
		this.bitMapKey = bitMapKey.getBytes(StandardCharsets.UTF_8);
		this.redisExecutor = redisExecutor;
	}

	@Override
	protected void setbit(int hash) {
		redisExecutor.setbit(bitMapKey, hash, true);
	}

	@Override
	protected boolean getbit(int hash) {
		return redisExecutor.getbit(bitMapKey, hash);
	}

}