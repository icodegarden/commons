package io.github.icodegarden.commons.redis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.CustomizeReentrantLock;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisReentrantLock extends CustomizeReentrantLock {

	public RedisReentrantLock(RedisExecutor redisExecutor, String name, Long expireSeconds) {
		super(new RedisLock(redisExecutor, name, expireSeconds));
	}
}