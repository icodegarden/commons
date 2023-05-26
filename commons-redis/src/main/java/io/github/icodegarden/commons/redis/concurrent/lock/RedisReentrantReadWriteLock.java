package io.github.icodegarden.commons.redis.concurrent.lock;

import java.util.UUID;

import io.github.icodegarden.commons.lang.concurrent.lock.CustomizeReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantReadWriteLock;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisReentrantReadWriteLock implements DistributedReentrantReadWriteLock {

	private final ReentrantLock readLock;
	private final ReentrantLock writeLock;

	public RedisReentrantReadWriteLock(RedisExecutor redisExecutor, String name, Long expireSeconds) {
		String identifier = UUID.randomUUID().toString();
		readLock = new ReentrantLock(redisExecutor, name, identifier, expireSeconds, true);
		writeLock = new ReentrantLock(redisExecutor, name, identifier, expireSeconds, false);
	}

	private class ReentrantLock extends CustomizeReentrantLock {

		public ReentrantLock(RedisExecutor redisExecutor, String name, String identifier, Long expireSeconds,
				boolean readType) {
			super(new RedisReadWriteLock(redisExecutor, name, identifier, expireSeconds, readType));
		}
	}

	@Override
	public DistributedReentrantLock readLock() {
		return readLock;
	}

	@Override
	public DistributedReentrantLock writeLock() {
		return writeLock;
	}

}
