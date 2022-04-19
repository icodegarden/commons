package io.github.icodegarden.commons.test.concurrent.lock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class DistributedLockTests {

	protected abstract DistributedLock newDistributedLock(String name);
	
	@Test
	void isAcquired() throws Exception {
		DistributedLock lock = newDistributedLock("lock");
		
		lock.acquire(1000);
		Assertions.assertThat(lock.isAcquired()).isTrue();
		
		lock.release();
	}
	/**
	 * 相同线程不可重入
	 * @throws Exception
	 */
	@Test
	void lock() throws Exception {
		DistributedLock lock = newDistributedLock("lock");

		Assertions.assertThat(lock.acquire(1000)).isTrue();
		Assertions.assertThat(lock.acquire(1000)).isFalse();// 不可再获取
		
		lock.release();
	}
	
	/**
	 * 不同锁名称
	 * @throws Exception
	 */
	@Test
	void lock_notSameName() throws Exception {
		DistributedLock lock1 = newDistributedLock("biz1");
		DistributedLock lock2 = newDistributedLock("biz2");

		Assertions.assertThat(lock1.acquire(1000)).isTrue();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 可获取
		
		lock1.release();
		lock2.release();
	}
	
}
