package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;
import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;

/**
 * <h1>支持Reentrant的互斥锁</h1> 只在相同对象、相同线程是可重入的，不同对象、不同线程一律互斥<br>
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperReentrantLock extends CuratorSupportLock implements DistributedReentrantLock {

	private final InterProcessMutex lock;

	/**
	 * 
	 * @param client
	 * @param root
	 * @param name 锁业务name，竞争锁的业务使用相同name
	 */
	public ZooKeeperReentrantLock(CuratorFramework client, String root, String name) {
		super(client);
		lock = new InterProcessMutex(client, root + "/locks/" + name + "/reentrant");
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
