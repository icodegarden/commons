//package io.github.icodegarden.commons.zookeeper.concurrent.lock;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.concurrent.TimeUnit;
//
//import org.apache.curator.RetryPolicy;
//import org.apache.curator.framework.CuratorFramework;
//import org.apache.curator.framework.CuratorFrameworkFactory;
//import org.apache.curator.framework.recipes.locks.InterProcessMutex;
//import org.apache.curator.retry.RetryOneTime;
//import org.apache.zookeeper.client.ZKClientConfig;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import io.github.icodegarden.commons.zookeeper.PropertiesConfig;
//import io.github.icodegarden.commons.zookeeper.concurrent.lock.ZooKeeperReentrantLock;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public class ZooKeeperReentrantLockTests_bak extends PropertiesConfig {
//	String root = "/zklock-test";
//	CuratorFramework client;
//
//	@BeforeEach
//	void init() {
//		RetryPolicy retryPolicy = new RetryOneTime(100);
//		ZKClientConfig zkClientConfig = new ZKClientConfig();
//		zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL, "zookeeper/" + zkConnectString);
//		client = CuratorFrameworkFactory.newClient(zkConnectString, 3000, 1000, retryPolicy, zkClientConfig);
//		client.start();
//	}
//	
//	@AfterEach
//	void close() {
//		client.close();
//	}
//	
//	/**
//	 * 相同线程，相同对象是可以重入的，不同对象不可再获取
//	 * @throws Exception
//	 */
//	@Test
//	void lock_sameThread() throws Exception {
//		ZooKeeperReentrantLock lock1 = new ZooKeeperReentrantLock(client, root,"lock");
//		ZooKeeperReentrantLock lock2 = new ZooKeeperReentrantLock(client, root,"lock");
//
//		Assertions.assertThat(lock1.acquire(1000)).isTrue();
//		Assertions.assertThat(lock1.acquire(1000)).isTrue();// 可以获取
//		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 不可以获取
//		
//		lock1.release();
//	}
//	
//	/**
//	 * 不同线程即使相同对象也是互斥的
//	 * @throws Exception
//	 */
//	@Test
//	void lock_notSameThread() throws Exception {
//		Object t1 = new Object();
//		Object t2 = new Object();
//
//		ZooKeeperReentrantLock lock = new ZooKeeperReentrantLock(client, root,"lock");
//		new Thread() {
//			public void run() {
//				// A
//				Assertions.assertThat(lock.acquire(1000)).isTrue();
//				synchronized (t1) {
//					t1.notify();
//				}
//
//				// B
//				synchronized (t2) {// 等待lock2获取，获取不到
//					try {
//						t2.wait();
//					} catch (InterruptedException e) {
//					}
//				}
//
//				// C
//				lock.release();
//			};
//		}.start();
//
//		synchronized (t1) {// A 等待lock1获取
//			t1.wait();
//		}
//
//		// B
//		Assertions.assertThat(lock.acquire(1000)).isFalse();// 无法获取
//		synchronized (t2) {
//			t2.notify();
//		}
//
//		// C
//		Thread.sleep(100);// 等待lock1执行释放
//		Assertions.assertThat(lock.acquire(1000)).isTrue();// 可以获取了
//		lock.release();
//	}
//	
//	/**
//	 * 即使使用不同zk客户端，也是跟相同zk客户端一样的结果，原理是不同线程或获取锁时先查看节点是否存在并是否属于本线程，存在且不属于就不能获取
//	 * @throws Exception
//	 */
//	@Test
//	void lock_notSameThread_notSameClient() throws Exception {
//		RetryPolicy retryPolicy1 = new RetryOneTime(100);
//		ZKClientConfig zkClientConfig1 = new ZKClientConfig();
//		zkClientConfig1.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL, "zookeeper/" + zkConnectString);
//		CuratorFramework client1 = CuratorFrameworkFactory.newClient(zkConnectString, 3000, 1000, retryPolicy1, zkClientConfig1);
//		client1.start();
//		
//		RetryPolicy retryPolicy2 = new RetryOneTime(100);
//		ZKClientConfig zkClientConfig2 = new ZKClientConfig();
//		zkClientConfig2.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL, "zookeeper/" + zkConnectString);
//		CuratorFramework client2 = CuratorFrameworkFactory.newClient(zkConnectString, 3000, 1000, retryPolicy2, zkClientConfig2);
//		client2.start();
//		
//		Object t1 = new Object();
//		Object t2 = new Object();
//
//		new Thread() {
//			public void run() {
//				// A
//				ZooKeeperReentrantLock lock1 = new ZooKeeperReentrantLock(client1, root,"lock");
//				Assertions.assertThat(lock1.acquire(1000)).isTrue();
//				synchronized (t1) {
//					t1.notify();
//				}
//
//				// B
//				synchronized (t2) {// 等待lock2获取，获取不到
//					try {
//						t2.wait();
//					} catch (InterruptedException e) {
//					}
//				}
//
//				// C
//				lock1.release();
//			};
//		}.start();
//
//		synchronized (t1) {// A 等待lock1获取
//			t1.wait();
//		}
//		// B
//		ZooKeeperReentrantLock lock2 = new ZooKeeperReentrantLock(client2, root,"lock");
//		Assertions.assertThat(lock2.acquire(1000)).isFalse();// 无法获取
//		synchronized (t2) {
//			t2.notify();
//		}
//
//		// C
//		Thread.sleep(100);// 等待lock1执行释放
//		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 可以获取了
//		lock2.release();
//	}
//}
