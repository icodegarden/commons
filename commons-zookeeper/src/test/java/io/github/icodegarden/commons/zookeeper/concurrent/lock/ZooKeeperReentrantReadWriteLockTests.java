package io.github.icodegarden.commons.zookeeper.concurrent.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.client.ZKClientConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedReentrantReadWriteLock;
import io.github.icodegarden.commons.test.concurrent.lock.DistributedReentrantReadWriteLockTests;
import io.github.icodegarden.commons.zookeeper.PropertiesConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperReentrantReadWriteLockTests extends DistributedReentrantReadWriteLockTests {

	String root = "/zklock-test";
	CuratorFramework client;

	@BeforeEach
	void initClient() {
		RetryPolicy retryPolicy = new RetryOneTime(100);
		ZKClientConfig zkClientConfig = new ZKClientConfig();
		zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL,
				"zookeeper/" + PropertiesConfig.zkConnectString);
		client = CuratorFrameworkFactory.newClient(PropertiesConfig.zkConnectString, 3000, 1000, retryPolicy,
				zkClientConfig);
		client.start();
	}

	@AfterEach
	void closeClient() {
		client.close();
	}

	@Override
	protected DistributedReentrantReadWriteLock newLock(String name) {
		return new ZooKeeperReentrantReadWriteLock(client, root, name);
	}

	@Test
	void destory() throws Exception {
		ZooKeeperReentrantReadWriteLock zooKeeperReentrantReadWriteLock = new ZooKeeperReentrantReadWriteLock(client,
				root, "lock");
		zooKeeperReentrantReadWriteLock.readLock().acquire(1000);
		zooKeeperReentrantReadWriteLock.writeLock().acquire(1000);
		zooKeeperReentrantReadWriteLock.destory();
	}
}
