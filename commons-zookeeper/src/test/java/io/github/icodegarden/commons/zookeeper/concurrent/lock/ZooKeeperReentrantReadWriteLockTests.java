package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.client.ZKClientConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.zookeeper.PropertiesConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperReentrantReadWriteLockTests extends PropertiesConfig {
	String root = "/zklock-test";
	CuratorFramework client;

	@BeforeEach
	void init() {
		RetryPolicy retryPolicy = new RetryOneTime(100);
		ZKClientConfig zkClientConfig = new ZKClientConfig();
		zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL, "zookeeper/" + zkConnectString);
		client = CuratorFrameworkFactory.newClient(zkConnectString, 3000, 1000, retryPolicy, zkClientConfig);
		client.start();
	}

	@AfterEach
	void close() {
		client.close();
	}

	/**
	 * 读锁不同线程可共享可重入
	 * 
	 * @throws Exception
	 */
	@Test
	void readLock_shared() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(
						client, root,"lock");
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.readLock();
				boolean acquire = lock1.acquire(1000);
				acquire = acquire & lock1.acquire(1000);// 可重入
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				synchronized (t1) {
					t1.notify();
				}
				// 等待共享锁lock2也获取
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}.start();

		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root,"lock");
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.readLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isTrue();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 可重入

		lock2.release();
	}

	/**
	 * 写锁不同对象互斥
	 * 
	 * @throws Exception
	 */
	@Test
	void writeLock_multx() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(
						client, root,"lock");
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.writeLock();
				boolean acquire = lock1.acquire(1000);
				acquire = acquire & lock1.acquire(1000);// 可重入
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				/**
				 * 写锁相同线程不同对象，互斥，这里省略
				 */

				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root,"lock");
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();
	}

	/**
	 * 先获取写锁，读锁不可获取
	 * 
	 * @throws Exception
	 */
	@Test
	void writeLock_then_readLock_multx() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(
						client, root,"lock");
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.writeLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root,"lock");
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.readLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();
	}

	/**
	 * 先获取读锁，写锁不可获取
	 * 
	 * @throws Exception
	 */
	@Test
	void readLock_then_writeLock_allowed() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(
						client, root,"lock");
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.readLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root,"lock");
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();
	}

	@Test
	void destory() throws Exception {
		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root,"lock");
		zooKeeperReentrantReadWriteLock.readLock().acquire(1000);
		zooKeeperReentrantReadWriteLock.writeLock().acquire(1000);
		zooKeeperReentrantReadWriteLock.destory();
	}
}
