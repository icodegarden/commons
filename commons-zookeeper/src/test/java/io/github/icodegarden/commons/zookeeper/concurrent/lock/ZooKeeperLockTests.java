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

import io.github.icodegarden.commons.zookeeper.PropertiesConfig;
import io.github.icodegarden.commons.zookeeper.concurrent.lock.ZooKeeperLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperLockTests extends PropertiesConfig {
	String root = "/beecomb";
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
	
	@Test
	void isAcquired() throws Exception {
		ZooKeeperLock lock = new ZooKeeperLock(client, root,"lock");
		
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
		ZooKeeperLock lock = new ZooKeeperLock(client, root,"lock");

		Assertions.assertThat(lock.acquire(1000)).isTrue();
		Assertions.assertThat(lock.acquire(1000)).isFalse();// 不可再获取
		
		lock.release();
	}
	
	/**
	 * 相同线程不可重入
	 * @throws Exception
	 */
	@Test
	void lock_notSameName() throws Exception {
		ZooKeeperLock lock1 = new ZooKeeperLock(client, root,"biz1");
		ZooKeeperLock lock2 = new ZooKeeperLock(client, root,"biz2");

		Assertions.assertThat(lock1.acquire(1000)).isTrue();
		Assertions.assertThat(lock2.acquire(1000)).isTrue();// 可获取
		
		lock1.release();
		lock2.release();
	}
	
	@Test
	void destory() throws Exception {
		ZooKeeperLock lock = new ZooKeeperLock(client, root,"lock");

		Assertions.assertThat(lock.acquire(1000)).isTrue();

		lock.destory();
	}
}
