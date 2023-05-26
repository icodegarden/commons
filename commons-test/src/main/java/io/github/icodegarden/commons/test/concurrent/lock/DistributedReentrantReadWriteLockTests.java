package io.github.icodegarden.commons.test.concurrent.lock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantReadWriteLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DistributedReentrantReadWriteLockTests {

	protected abstract DistributedReentrantReadWriteLock newLock(String name);

	/**
	 * 读锁全程共享可重入
	 * 
	 * @throws Exception
	 */
	@Test
	void readLock_shared() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				DistributedReentrantReadWriteLock lock = newLock("lock");
				DistributedLock lock1 = lock.readLock();
				boolean acquire = lock1.acquire(1000);
				acquire = acquire & lock1.acquire(1000);// 可重入
				if (!acquire) {
					System.out.println("acquire failed in thread - A");
					System.exit(-1);
				}
				
				if (!lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - B");
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

		DistributedReentrantReadWriteLock lock = newLock("lock");
		DistributedLock lock2 = lock.readLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isTrue();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 可重入
		Assertions.assertThat(lock2.isAcquired()).isTrue();

		lock2.release();
	}

	/**
	 * 写锁全程互斥可重入
	 * 
	 * @throws Exception
	 */
	@Test
	void writeLock_multx() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				DistributedReentrantReadWriteLock lock = newLock("lock");
				DistributedLock lock1 = lock.writeLock();
				boolean acquire = lock1.acquire(1000);
				acquire = acquire & lock1.acquire(1000);// 可重入
				if (!acquire) {
					System.out.println("acquire failed in thread - C");
					System.exit(-1);
				}
				
				if (!lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - D");
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

		DistributedReentrantReadWriteLock lock = newLock("lock");
		DistributedLock lock2 = lock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 互斥
		Assertions.assertThat(lock2.isAcquired()).isFalse();
	}

	/**
	 * 先获取写锁，在写锁中可以获取读锁，其他线程中互斥
	 * 
	 * @throws Exception
	 */
	@Test
	void writeLock_then_readLock_multx() throws Exception {
		Object t1 = new Object();

		new Thread() {
			@Override
			public void run() {
				DistributedReentrantReadWriteLock lock = newLock("lock");
				DistributedLock lock1 = lock.writeLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread - E");
					System.exit(-1);
				}
				
				if (!lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - F");
					System.exit(-1);
				}
				
				lock1 = lock.readLock();
				acquire = lock1.acquire(1000);// 在写锁中可以获取读锁
				acquire = acquire & lock1.acquire(1000);// 可重入
				if (!acquire) {
					System.out.println("acquire failed in thread - G");
					System.exit(-1);
				}
				
				if (!lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - H");
					System.exit(-1);
				}
				
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		DistributedReentrantReadWriteLock lock = newLock("lock");
		DistributedLock lock2 = lock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 互斥
		Assertions.assertThat(lock2.isAcquired()).isFalse();
		lock2 = lock.readLock();
		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 互斥
		Assertions.assertThat(lock2.isAcquired()).isFalse();
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
				DistributedReentrantReadWriteLock lock = newLock("lock");
				DistributedLock lock1 = lock.readLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread - I");
					System.exit(-1);
				}
				
				if (!lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - J");
					System.exit(-1);
				}
				
				lock1 = lock.writeLock();
				acquire = lock1.acquire(1000);// 在读锁中不能获取写锁
				if (acquire) {
					System.out.println("acquire error in thread - K");
					System.exit(-1);
				}
				
				if (lock1.isAcquired()) {
					System.out.println("isAcquired failed in thread - L");
					System.exit(-1);
				}
				
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		DistributedReentrantReadWriteLock lock = newLock("lock");
		DistributedLock lock2 = lock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 互斥
		Assertions.assertThat(lock2.isAcquired()).isFalse();
		lock2 = lock.readLock();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 共享
		Assertions.assertThat(lock2.isAcquired()).isTrue();
	}

}