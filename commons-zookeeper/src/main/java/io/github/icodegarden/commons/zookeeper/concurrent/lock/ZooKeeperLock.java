package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;

/**
 * <h1>不支持Reentrant的互斥锁</h1> 全程互斥<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperLock extends CuratorSupportLock implements DistributedLock {

	private final InterProcessSemaphoreMutex lock;

	/**
	 * 
	 * @param client
	 * @param root
	 * @param name 锁业务name，竞争锁的业务使用相同name
	 */
	public ZooKeeperLock(CuratorFramework client, String root, String name) {
		super(client);
		lock = new InterProcessSemaphoreMutex(client, root + "/locks/" + name + "/not_reentrant");
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
