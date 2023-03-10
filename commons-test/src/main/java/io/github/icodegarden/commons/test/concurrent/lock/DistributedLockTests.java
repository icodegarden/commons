package io.github.icodegarden.commons.test.concurrent.lock;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	protected abstract long getExpireSeconds();
	
	@Test
	void isAcquired() throws Exception {
		DistributedLock lock = newDistributedLock("lock");
		
		lock.acquire(1000);
		Assertions.assertThat(lock.isAcquired()).isTrue();
		
		lock.release();
		
		Assertions.assertThat(lock.isAcquired()).isFalse();
	}
	
	/**
	 * 阻塞直到过期后会获取
	 */
	@Test
	void acquire() throws Exception {
		DistributedLock lock = newDistributedLock("lock");
		
		if(lock.getClass().getName().contains("ZooKeeper")) {
			return;//zk不会过期，不测
		}

		Assertions.assertThat(lock.acquire(1000)).isTrue();
		lock.acquire();
		
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
		
		Assertions.assertThat(lock.acquire(1000)).isTrue();// release后可再获取
		lock.release();
		
		Thread.sleep(getExpireSeconds());
		Assertions.assertThat(lock.acquire(1000)).isTrue();// 等待过期后可再获取
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
	
	/**
	 * 并发获取锁
	 */
	@Test
	void concurrentAcquire() throws Exception {
		ExecutorService threadPool = Executors.newFixedThreadPool(1000);
		String ln = null;
		
		int loopCount = 10;
		for(int loop=0;loop<loopCount;loop++) {//多轮测试
			System.out.println(this.getClass().getName()+" concurrentAcquire loop:"+loop);
			
			if(loop < loopCount/2) {
				ln = System.currentTimeMillis()+"";//前1半用争抢新锁，后半用已存在的锁，以此验证语句都没问题
			}
			
			final String lockname = ln;
			
			int count = 200;//每轮线程数
			CountDownLatch countDownLatch = new CountDownLatch(count);
			
			List<DistributedLock> list = new CopyOnWriteArrayList<>();
			for(int i=0;i<count;i++) {
				threadPool.execute(()->{
					try {
						DistributedLock lock = newDistributedLock(lockname);
						if(lock.acquire(1000)) {
							list.add(lock);
//							lock.release();	不能在此release，会被其他线程拿到锁
						}
					}finally {
						countDownLatch.countDown();
					}
				});
			}
			countDownLatch.await();
			Assertions.assertThat(list.size()).isEqualTo(1);//每轮都只有1个线程可以争抢到
			
			list.get(0).release();//最后释放锁
		}
	}
}
