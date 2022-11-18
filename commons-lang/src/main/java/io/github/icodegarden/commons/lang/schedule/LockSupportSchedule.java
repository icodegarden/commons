package io.github.icodegarden.commons.lang.schedule;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class LockSupportSchedule extends GracefullyShutdownSchedule {

	protected final DistributedLock lock;
	private long acquireLockTimeoutMillis = 1000;

	public LockSupportSchedule(DistributedLock lock) {
		this.lock = lock;
	}

	public void setAcquireLockTimeoutMillis(long acquireLockTimeoutMillis) {
		this.acquireLockTimeoutMillis = acquireLockTimeoutMillis;
	}

	@Override
	protected void doSchedule() {
		if (lock.acquire(acquireLockTimeoutMillis)) {
			try {
				doScheduleAfterLocked();
			} finally {
				lock.release();
			}
		} else {
			if (log.isInfoEnabled() && allowLoopLog()) {
				log.info("{} acquire lock false", this.getClass().getSimpleName());
			}
		}
	}

	protected abstract void doScheduleAfterLocked();

	@Override
	public void shutdown() {
		super.shutdown();
		if (lock.isAcquired()) {
			lock.release();
		}
	}
}