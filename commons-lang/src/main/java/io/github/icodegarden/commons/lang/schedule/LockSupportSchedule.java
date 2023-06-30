package io.github.icodegarden.commons.lang.schedule;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
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
		super();
		this.lock = lock;
	}
	
	public LockSupportSchedule(String name, DistributedLock lock) {
		super(name);
		this.lock = lock;
	}

	public void setAcquireLockTimeoutMillis(long acquireLockTimeoutMillis) {
		this.acquireLockTimeoutMillis = acquireLockTimeoutMillis;
	}

	@Override
	protected void doSchedule() throws Throwable {
		if (lock.acquire(acquireLockTimeoutMillis)) {
			try {
				doScheduleAfterLocked();
			} finally {
				lock.release();
			}
		} else {
			if (log.isInfoEnabled()) {
				log.info("{} acquire lock false, host:{}", this.getClass().getSimpleName(), SystemUtils.getIp());
			}
		}
	}

	protected abstract void doScheduleAfterLocked() throws Throwable;

	@Override
	public void close() {
		LogUtils.infoIfEnabled(log, () -> log.info("{} start close.", getName()));
		
		super.close();
		
		if (lock.isAcquired()) {
			lock.release();
		}
		
		LogUtils.infoIfEnabled(log, () -> log.info("{} complete close.", getName()));
	}
}