package io.github.icodegarden.commons.lang.concurrent.lock;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseReadWriteLockRepository.LockDO;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DatabaseReadWriteLock implements DistributedLock {

	private final boolean readType;

	private final String identifier;

	private final DatabaseReadWriteLockRepository lockRepository;

	private final String name;
	private final Long expireSeconds;

	private long acquireIntervalMillis = 500;

	public DatabaseReadWriteLock(DatabaseReadWriteLockRepository lockRepository, String name, String identifier, Long expireSeconds,
			boolean readType) {
		Assert.hasText(name, "name must not empty");
		Assert.isTrue(name.length() <= 50, "name length must <= 50");

		this.lockRepository = lockRepository;
		this.identifier = identifier;
		this.name = name;
		this.expireSeconds = expireSeconds;
		this.readType = readType;
	}

	public void setAcquiredIntervalMillis(long acquireIntervalMillis) {
		Assert.isTrue(acquireIntervalMillis > 0, "acquireIntervalMillis must gt 0");
		this.acquireIntervalMillis = acquireIntervalMillis;
	}

	@Override
	public boolean isAcquired() throws LockException {
		try {
			String nowStr = SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now());
			List<LockDO> list = lockRepository.listLockedDataInterProcess(name, identifier, readType, nowStr);
			if (list.isEmpty()) {
				return false;
			}
			return list.stream().anyMatch(one -> one.getIdentifier().equals(identifier) && one.isReadType() == readType);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	@Override
	public void acquire() throws LockException {
		acquire(Long.MAX_VALUE);
	}

//获取读锁->
//	检查本地是否已获取->
//		是->成功+1
//		否->获取锁名称所有lock条数->
//			检查是否有wirte锁->
//				是->write锁是否本地持有的-》
//					是->成功，写入read锁
//					否-》失败
//				否->写入read锁
//					
//释放读锁->删除对应lock数据	
//
//获取写锁->
//    检查本地是否已获取->
//		是-》成功+1
//		否-》获取锁名称所有lock条数->
//			检查是否有任何锁-》
//				是-》失败
//				否-》写入write锁
//				
//释放写锁-》删除对应lock数据					
	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		LocalDateTime start = SystemUtils.now();
		for (;;) {
			try {
				boolean b = mutex();
				if (!b) {
					lockRepository.createRow(name, identifier, expireSeconds,
							SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()), readType);
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

	private boolean mutex() {
		String nowStr = SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now());
		List<LockDO> dos = lockRepository.listLockedDatas(name, nowStr);
		if (this.readType) {
			/**
			 * 是否有write锁且不是本地持有的
			 */
			boolean b = dos.stream().anyMatch(one -> {
				return !one.isReadType() && !identifier.equals(one.getIdentifier());
			});

			return b;
		} else {
			/**
			 * 是否有任何锁
			 */
			boolean b = !dos.isEmpty();
			return b;
		}
	}

	@Override
	public void release() throws LockException {
		try{
			lockRepository.deleteRow(name, identifier);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
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
