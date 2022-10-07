package io.github.icodegarden.commons.redis.concurrent.lock;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockInterruptedException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.redis.RedisExecutor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisLock implements DistributedLock {

	private static final Charset CHARSET = Charset.forName("utf-8");

	private static final byte[] SCRIPT = "local v = redis.call('setnx',KEYS[1],ARGV[1]);if(v==1) then redis.call('expire',KEYS[1],ARGV[2]); end;return v;"
			.getBytes(CHARSET);

	private final RedisExecutor redisExecutor;

	private final byte[] identifier = UUID.randomUUID().toString().getBytes(CHARSET);

	private final byte[] key;
	private final byte[] expireSecondsBytes;

	private long acquireIntervalMillis = 100;

	/**
	 * 
	 * @param redisExecutor
	 * @param name          锁业务name，竞争锁的业务使用相同name
	 * @param expireSeconds 过期时间
	 */
	public RedisLock(RedisExecutor redisExecutor, String name, Long expireSeconds) {
		this.redisExecutor = redisExecutor;
		this.key = name.getBytes(CHARSET);
		expireSecondsBytes = Long.toString(expireSeconds).getBytes(CHARSET);
	}

	public void setAcquiredIntervalMillis(long acquireIntervalMillis) {
		Assert.isTrue(acquireIntervalMillis > 0, "acquireIntervalMillis must gt 0");
		this.acquireIntervalMillis = acquireIntervalMillis;
	}

	@Override
	public boolean isAcquired() throws LockException {
		try {
			byte[] bs = redisExecutor.get(key);
			return bs != null && Arrays.equals(bs, identifier);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	@Override
	public void acquire() throws LockException {
		for (;;) {
			boolean acquire = acquire(Long.MAX_VALUE);
			if (!acquire) {
				sleep();
			}
		}
	}

	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		LocalDateTime start = SystemUtils.now();
		for (;;) {
			try {
				Object obj = redisExecutor.eval(SCRIPT, 1, key, identifier, expireSecondsBytes);
				// redisTemplate返回的是boolean
				if (obj instanceof Boolean) {
					return (Boolean) obj;
				}

				// 这里返回类型是Long的原因是redis直接返回的0或1，而不是设置进去的
				Long result = (Long) obj;

				boolean success = result == 1;

				if (success) {
					return true;
				}

				if (SystemUtils.now().minus(timeoutMillis, ChronoUnit.MILLIS).isAfter(start)) {
					return false;
				}
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
			sleep();
		}
	}

	@Override
	public void release() throws LockException {
		/**
		 * 必须要检查
		 */
		if (isAcquired()) {
			try {
				redisExecutor.del(key);
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
		}
	}

	private void sleep() throws LockInterruptedException {
		try {
			Thread.sleep(acquireIntervalMillis);
		} catch (InterruptedException e) {
			throw new LockInterruptedException(e);
		}
	}
}
