package io.github.icodegarden.commons.redis.concurrent.lock;

import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseReadWriteLockDao;
import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcReentrantReadWriteLock;
import io.github.icodegarden.commons.redis.JedisPoolRedisExecutorTests;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.redis.jedis.JedisPoolRedisExecutor;
import io.github.icodegarden.commons.test.TestsDataSourceDependent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisReentrantReadWriteLockTests {

	@BeforeEach
	void init() {
		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		
		Set<byte[]> keys = redisExecutor.keys("*".getBytes());
		keys.forEach(key->{
			redisExecutor.del(key);	
		});
		
	}

	@AfterEach
	void close() {
	}

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
				RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
				RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
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

		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
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
				RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
				RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
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

		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();//互斥
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
				RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
				RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.writeLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				lock1 = zooKeeperReentrantReadWriteLock.readLock();
				acquire = lock1.acquire(1000);//在写锁中可以获取读锁
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();//互斥
		lock2 = zooKeeperReentrantReadWriteLock.readLock();
		Assertions.assertThat(lock2.acquire(1000)).isFalse();//互斥
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
				RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
				RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
				DistributedLock lock1 = zooKeeperReentrantReadWriteLock.readLock();
				boolean acquire = lock1.acquire(1000);
				if (!acquire) {
					System.out.println("acquire failed in thread");
					System.exit(-1);
				}
				lock1 = zooKeeperReentrantReadWriteLock.writeLock();
				acquire = lock1.acquire(1000);//在写锁中可以获取读锁
				if (acquire) {
					System.out.println("acquire error in thread");
					System.exit(-1);
				}
				synchronized (t1) {
					t1.notify();
				}
			}
		}.start();

		RedisExecutor redisExecutor = new JedisPoolRedisExecutor(JedisPoolRedisExecutorTests.newJedisPool());
		RedisReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new RedisReentrantReadWriteLock(redisExecutor, "lock", 5L);
		DistributedLock lock2 = zooKeeperReentrantReadWriteLock.writeLock();

		synchronized (t1) {// 等待lock1获取
			try {
				t1.wait();
			} catch (InterruptedException e) {
			}
		}

		Assertions.assertThat(lock2.acquire(1000)).isFalse();//互斥
		lock2 = zooKeeperReentrantReadWriteLock.readLock();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();//共享
	}

}
