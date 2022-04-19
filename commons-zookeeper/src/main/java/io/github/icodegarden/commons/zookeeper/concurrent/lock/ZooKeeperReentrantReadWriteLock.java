package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantReadWriteLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperReentrantReadWriteLock implements DistributedReentrantReadWriteLock {

	private final ReentrantLock readLock;
	private final ReentrantLock writeLock;

	/**
	 * 
	 * @param client
	 * @param root
	 * @param name   锁业务name，竞争锁的业务使用相同name
	 */
	public ZooKeeperReentrantReadWriteLock(CuratorFramework client, String root, String name) {
		if (CuratorFrameworkState.LATENT == client.getState()) {
			synchronized (client) {
				if (CuratorFrameworkState.LATENT == client.getState()) {
					client.start();
				}
			}
		}

		InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client,
				root + "/locks/" + name + "/reentrant_read_write");

		readLock = new ReentrantLock(client, lock.readLock());
		writeLock = new ReentrantLock(client, lock.writeLock());
	}

	@Override
	public DistributedReentrantLock readLock() {
		return readLock;
	}

	@Override
	public DistributedReentrantLock writeLock() {
		return writeLock;
	}

	public void destory() {
		readLock.destory();
		writeLock.destory();
	}

	private class ReentrantLock extends CuratorSupportLock implements DistributedReentrantLock {
		private final InterProcessMutex lock;

		public ReentrantLock(CuratorFramework client, InterProcessMutex lock) {
			super(client);
			this.lock = lock;
		}

		@Override
		public boolean isAcquired() {
			return lock.isAcquiredInThisProcess();
		}

		@Override
		public void acquire() {
			try {
				lock.acquire();
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
		}

		@Override
		public boolean acquire(long timeoutMillis) {
			try {
				return lock.acquire(timeoutMillis, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
		}

		@Override
		public void release() {
			try {
				lock.release();
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
		}
	}
}
