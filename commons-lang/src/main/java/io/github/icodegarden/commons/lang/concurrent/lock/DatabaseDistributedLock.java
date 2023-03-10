package io.github.icodegarden.commons.lang.concurrent.lock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DatabaseDistributedLock implements DistributedLock {

	public static final String TABLE_NAME = "distributed_lock";

	private final String identifier = UUID.randomUUID().toString();

	private final DatabaseDistributedLockDao lockDao;

	private final String name;
	private final Long expireSeconds;

	private long acquireIntervalMillis = 500;

	public DatabaseDistributedLock(DatabaseDistributedLockDao lockDao, String name, Long expireSeconds) {
		Assert.hasText(name, "name must not empty");
		Assert.isTrue(name.length() <= 20, "name length must <= 20");

		this.lockDao = lockDao;
		this.name = name;
		this.expireSeconds = expireSeconds;
	}

	public void setAcquiredIntervalMillis(long acquireIntervalMillis) {
		Assert.isTrue(acquireIntervalMillis > 0, "acquireIntervalMillis must gt 0");
		this.acquireIntervalMillis = acquireIntervalMillis;
	}

	@Override
	public boolean isAcquired() throws LockException {
		try {
			String nowStr = SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now());
			String value = lockDao.getLockedIdentifier(name, nowStr);
			return value != null && value.equals(identifier);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	@Override
	public void acquire() throws LockException {
		acquire(Long.MAX_VALUE);
	}

	/**
	 * 先查询lock name对应的数据是否已存在<br>
	 * 存在则使用update ... where name=? and (is_locked = 0 or
	 * lock_at+timeout<now )更新，更新结果=0说明获取锁失败，否则成功<br> 不存在则新增，新增被唯一约束说明获取锁失败，否则成功<br>
	 */
	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		LocalDateTime start = SystemUtils.now();
		for (;;) {
			try {
				if (lockDao.existsRow(name) == 1) {
					int rows = lockDao.updateLocked(name, identifier, expireSeconds,
							SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
					if (rows == 1) {
						return true;
					}
				} else {
					try {
						lockDao.createRow(name, identifier, expireSeconds,
								SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
						return true;
					} catch (DuplicateKeyException e) {
						// 继续下一轮
					}
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
			lockDao.updateRelease(name);
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
