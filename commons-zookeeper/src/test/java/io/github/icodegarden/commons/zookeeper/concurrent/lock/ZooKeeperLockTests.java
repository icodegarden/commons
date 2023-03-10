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
import io.github.icodegarden.commons.test.concurrent.lock.DistributedLockTests;
import io.github.icodegarden.commons.zookeeper.PropertiesConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ZooKeeperLockTests extends DistributedLockTests {

	String root = "/zklock-test";
	CuratorFramework client;

	@BeforeEach
	void init() {
		RetryPolicy retryPolicy = new RetryOneTime(100);
		ZKClientConfig zkClientConfig = new ZKClientConfig();
		zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL,
				"zookeeper/" + PropertiesConfig.zkConnectString);
		client = CuratorFrameworkFactory.newClient(PropertiesConfig.zkConnectString, 3000, 1000, retryPolicy,
				zkClientConfig);
		client.start();
	}

	@AfterEach
	void close() {
		client.close();
	}

	@Override
	protected DistributedLock newDistributedLock(String name) {
		return new ZooKeeperLock(client, root, name);
	}
	
	@Override
	protected long getExpireSeconds() {
		return 1;
	}

	@Test
	void destory() throws Exception {
		ZooKeeperLock lock = new ZooKeeperLock(client, root, "lock");

		Assertions.assertThat(lock.acquire(1000)).isTrue();

		lock.destory();
	}
}
