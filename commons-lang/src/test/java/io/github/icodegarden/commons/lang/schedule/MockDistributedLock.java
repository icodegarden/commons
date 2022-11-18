package io.github.icodegarden.commons.lang.schedule;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MockDistributedLock implements DistributedLock {
	
	private boolean acquired;

	@Override
	public void release() throws LockException {
		acquired = false;
	}

	@Override
	public boolean isAcquired() throws LockException {
		return acquired;
	}

	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		if (acquired) {
			return false;
		}
		acquired = true;
		return true;
	}

	@Override
	public void acquire() throws LockException {
		throw new RuntimeException("NOT SUPPORT");
	}
}