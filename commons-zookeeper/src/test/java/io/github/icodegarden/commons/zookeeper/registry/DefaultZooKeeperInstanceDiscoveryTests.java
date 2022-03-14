package io.github.icodegarden.commons.zookeeper.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.zookeeper.CommonZookeeperBuilder;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class DefaultZooKeeperInstanceDiscoveryTests extends CommonZookeeperBuilder {

	@Test
	void listInstancesMasters() throws Exception {
		ZooKeeperInstanceDiscovery instanceDiscovery = new ZooKeeperInstanceDiscovery.Default(zkh, "/beecomb");
		List<ZooKeeperRegisteredInstance> masters = instanceDiscovery.listInstances("master");
		assertThat(masters).isEmpty();

		ZooKeeperInstanceRegistry instanceRegistry = new ZooKeeperInstanceRegistry(zkh, "/beecomb", "master", 9999);
		instanceRegistry.registerIfNot();

		masters = instanceDiscovery.listInstances("master");
		assertThat(masters).hasSize(1);
	}

	@Test
	void listInstancesWorkers() throws Exception {
		ZooKeeperInstanceDiscovery instanceDiscovery = new ZooKeeperInstanceDiscovery.Default(zkh, "/beecomb");
		List<ZooKeeperRegisteredInstance> workers = instanceDiscovery.listInstances("worker");
		assertThat(workers).isEmpty();

		ZooKeeperInstanceRegistry instanceRegistry = new ZooKeeperInstanceRegistry(zkh, "/beecomb", "worker", 9999);
		instanceRegistry.registerIfNot();

		workers = instanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(1);

	}
}
