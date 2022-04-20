package io.github.icodegarden.commons.zookeeper.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.zookeeper.CommonZookeeperBuilder;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class NamesWatchedZooKeeperInstanceDiscoveryTests extends CommonZookeeperBuilder {

	String root = "/xff";
	
	@Test
	void listInstances() throws Exception {
		ZooKeeperInstanceDiscovery instanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zkh, root);
		NamesWatchedZooKeeperInstanceDiscovery namesWatchedZooKeeperInstanceDiscovery = new NamesWatchedZooKeeperInstanceDiscovery(
				instanceDiscovery, zkh, root, Arrays.asList("worker"), Long.MAX_VALUE/* 时间为永不刷新，便于测试watch事件 */);

		// --------------------------------初始没实例
		List<ZooKeeperRegisteredInstance> workers = namesWatchedZooKeeperInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(0);

		// --------------------------------新增1个实例，断言1个，因为触发了created事件
		ZooKeeperInstanceRegistry instanceRegistry1 = new ZooKeeperInstanceRegistry(zkh, root, "worker", 10000);
		ZooKeeperRegisteredInstance instance1 = instanceRegistry1.registerIfNot();

		Thread.sleep(200);// 事件触发需要一点点时间

		workers = namesWatchedZooKeeperInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isEqualTo(instance1);

		// --------------------------------再新增1个实例，断言2个，因为触发了created事件
		ZooKeeperInstanceRegistry instanceRegistry2 = new ZooKeeperInstanceRegistry(zkh, root, "worker", 10000);
		ZooKeeperRegisteredInstance instance2 = instanceRegistry2.registerIfNot();

		Thread.sleep(200);// 事件触发需要一点点时间

		workers = namesWatchedZooKeeperInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(2);
		assertThat(workers.get(0)).isEqualTo(instance1);
		assertThat(workers.get(1)).isEqualTo(instance2);

		// --------------------------------注销实例1，断言1个，因为触发了deleted事件
		instanceRegistry1.deregister();

		Thread.sleep(200);// 事件触发需要一点点时间

		workers = namesWatchedZooKeeperInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0)).isEqualTo(instance2);

		// --------------------------------注销实例2，断言0个，因为触发了deleted事件
		instanceRegistry2.deregister();

		Thread.sleep(200);// 事件触发需要一点点时间

		workers = namesWatchedZooKeeperInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(0);
	}
}
