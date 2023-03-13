package io.github.icodegarden.commons.lang.concurrent.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CustomizeReentrantLock implements DistributedReentrantLock {

	private final ConcurrentMap<Thread, AtomicInteger> threadData = new ConcurrentHashMap<>();

	private final DistributedLock lock;

	public CustomizeReentrantLock(DistributedLock lock) {
		this.lock = lock;
	}

	@Override
	public boolean isAcquired() throws LockException {
		return lock.isAcquired();
	}

	@Override
	public void acquire() throws LockException {
		acquire(Long.MAX_VALUE);
	}

	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		/*
		 * see
		 * org.apache.curator.framework.recipes.locks.InterProcessMutex.acquire(long,
		 * TimeUnit)
		 */

		Thread currentThread = Thread.currentThread();

		AtomicInteger lockData = threadData.get(currentThread);
		if (lockData != null) {
			// re-entering
			lockData.incrementAndGet();
			return true;
		}

		boolean b = lock.acquire(timeoutMillis);
		if (b) {
			/*
			 * 计数1<br> see
			 * org.apache.curator.framework.recipes.locks.InterProcessMutex.LockData构造
			 */
			AtomicInteger newLockData = new AtomicInteger(1);
			threadData.put(currentThread, newLockData);
			return true;
		}

		return false;
	}

	@Override
	public void release() throws LockException {
		// see org.apache.curator.framework.recipes.locks.InterProcessMutex.release()

		Thread currentThread = Thread.currentThread();
		AtomicInteger lockData = threadData.get(currentThread);
		if (lockData == null) {
			return;
		}

		int newLockCount = lockData.decrementAndGet();
		if (newLockCount > 0) {
			return;
		}
		if (newLockCount < 0) {
			return;
		}
		try {
			lock.release();
		} finally {
			threadData.remove(currentThread);
		}
	}
}
