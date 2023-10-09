package io.github.icodegarden.commons.zookeeper.metricsregistry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.zookeeper.CommonZookeeperBuilder;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZnodePatternZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperInstanceDiscovery;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.metricsregistry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZnodePatternZooKeeperInstanceDiscoveryTests extends CommonZookeeperBuilder {

	String root = "/xff";
	
	@Test
	void listInstancesMasters() throws Exception {
		ZooKeeperInstanceDiscovery instanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zkh, root);
		List<ZooKeeperRegisteredInstance> masters = instanceDiscovery.listInstances("master");
		assertThat(masters).isEmpty();

		ZooKeeperInstanceRegistry instanceRegistry = new ZooKeeperInstanceRegistry(zkh, root, "master", 9999);
		instanceRegistry.registerIfNot();

		masters = instanceDiscovery.listInstances("master");
		assertThat(masters).hasSize(1);
	}

	@Test
	void listInstancesWorkers() throws Exception {
		ZooKeeperInstanceDiscovery instanceDiscovery = new ZnodePatternZooKeeperInstanceDiscovery(zkh, root);
		List<ZooKeeperRegisteredInstance> workers = instanceDiscovery.listInstances("worker");
		assertThat(workers).isEmpty();

		ZooKeeperInstanceRegistry instanceRegistry = new ZooKeeperInstanceRegistry(zkh, root, "worker", 9999);
		instanceRegistry.registerIfNot();

		workers = instanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(1);

	}
}
