package io.github.icodegarden.commons.test.concurrent.lock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DistributedReentrantLockTests {

	protected abstract DistributedReentrantLock newLock(String name);

	/**
	 * 相同线程，相同对象是可以重入的，不同对象不可再获取
	 * 
	 * @throws Exception
	 */
	@Test
	void lock_sameThread() throws Exception {
		DistributedReentrantLock lock1 = newLock("lock");
		DistributedReentrantLock lock2 = newLock("lock");

		reentrant(lock1, lock2, 1000);
	}

	private void reentrant(DistributedReentrantLock lock1, DistributedReentrantLock lock2, long timeoutMillis) {
		Assertions.assertThat(lock1.acquire(1000)).isTrue();// 可以重入，可以获取
		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 不可以获取

		lock1.release();// reentrant的release只会减少1个计数
	}

	/**
	 * 不同线程即使相同对象也是互斥的
	 * 
	 * @throws Exception
	 */
	@Test
	void lock_notSameThread() throws Exception {
		Object t1 = new Object();
		Object t2 = new Object();

		DistributedReentrantLock lock = newLock("lock");
		new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}

				// A
				Assertions.assertThat(lock.acquire(1000)).isTrue();
				synchronized (t1) {
					t1.notify();
				}

				// B
				synchronized (t2) {// 等待lock2获取，但lock2获取不到
					try {
						t2.wait();
					} catch (InterruptedException e) {
					}
				}

				// C
				lock.release();
			};
		}.start();

		synchronized (t1) {// A 等待lock1获取成功
			t1.wait();
		}

		// B
		Assertions.assertThat(lock.acquire(1000)).isFalse();// 不同线程，无法获取
		synchronized (t2) {
			t2.notify();
		}

		// C
		Thread.sleep(100);// 等待lock1执行释放

		Assertions.assertThat(lock.acquire(1000)).isTrue();// 不同线程，可以获取了
		lock.release();
	}

	/**
	 * 以zk为例：即使使用不同zk客户端，也是跟相同zk客户端一样的结果，原理是不同线程或获取锁时先查看节点是否存在并是否属于本线程，存在且不属于就不能获取
	 * 
	 * @throws Exception
	 */
	@Test
	void lock_notSameThread_notSameClient() throws Exception {
		Object t1 = new Object();
		Object t2 = new Object();

		new Thread() {
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}

				// A
				DistributedReentrantLock lock1 = newLock("lock");
				Assertions.assertThat(lock1.acquire(1000)).isTrue();
				synchronized (t1) {
					t1.notify();
				}

				// B
				synchronized (t2) {// 等待lock2获取，但lock2获取不到
					try {
						t2.wait();
					} catch (InterruptedException e) {
					}
				}

				// C
				lock1.release();
			};
		}.start();

		synchronized (t1) {// A 等待lock1获取成功
			t1.wait();
		}

		// B
		DistributedReentrantLock lock2 = newLock("lock");
		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 不同线程，无法获取
		synchronized (t2) {
			t2.notify();
		}

		// C
		Thread.sleep(100);// 等待lock1执行释放

		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 不同线程，可以获取了
		lock2.release();
	}
}