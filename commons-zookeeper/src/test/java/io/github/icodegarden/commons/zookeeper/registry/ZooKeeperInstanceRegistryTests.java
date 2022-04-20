package io.github.icodegarden.commons.zookeeper.registry;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.registry.RegisteredInstance;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import io.github.icodegarden.commons.zookeeper.CommonZookeeperBuilder;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZooKeeperInstanceRegistryTests extends CommonZookeeperBuilder {

	String root = "/xff";
	int port = 9999;
	ZooKeeperInstanceRegistry instanceRegistry;

	@BeforeEach
	void init() {
		instanceRegistry = new ZooKeeperInstanceRegistry(zkh, root, "master", port);
	}
	
	@Test
	void registerIfNot() throws Exception {
		String ip = SystemUtils.getIp();

		RegisteredInstance result = instanceRegistry.registerIfNot();
		assertThat(result).isInstanceOf(ZooKeeperRegisteredInstance.class);
		assertThat(result).isNotNull();
		assertThat(result.getInstanceName()).startsWith(ip + ":" + port + "-");
		assertThat(((ZooKeeperRegisteredInstance) result).getZnode())
				.startsWith(root + "/master/instances/" + ip + ":" + port + "-");
		assertThat(result.getIp()).isEqualTo(ip);
		assertThat(result.getPort()).isEqualTo(port);
	}

	@Test
	void registerIfNot2() throws Exception {
		ZooKeeperRegisteredInstance registeredInstance = instanceRegistry.registerIfNot();
		assertThat(registeredInstance == instanceRegistry.registerIfNot()).isTrue();
//		assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> instanceRegistry.register())
//				.withMessageStartingWith("node was registered");
	}

	@Test
	void deregister() throws Exception {
		instanceRegistry.registerIfNot();
		assertThat(instanceRegistry.getRegistered()).isNotNull();
		instanceRegistry.deregister();
		assertThat(instanceRegistry.getRegistered()).isNull();
		instanceRegistry.registerIfNot();
		assertThat(instanceRegistry.getRegistered()).isNotNull();
	}
	
	@Test
	void buildRegisterDirPath() throws Exception {
		String path = ServiceNamePath.buildServiceNamePath("/beecomb", "worker");
		assertThat(path).isEqualTo("/beecomb/worker/instances");
		
		path = ServiceNamePath.buildServiceNamePath("/my/beecomb", "worker");
		assertThat(path).isEqualTo("/my/beecomb/worker/instances");
	}
	
	@Test
	void resovleRegisteredInstance() throws Exception {
		ZooKeeperRegisteredInstance instance = ZooKeeperInstanceRegistry
				.resovleRegisteredInstance("/beecomb/worker/instances/10.33.211.12:10000-0000000115");
		assertThat(instance).isNotNull();
		assertThat(instance.getServiceName()).isEqualTo("worker");
		assertThat(instance.getInstanceName()).isEqualTo("10.33.211.12:10000-0000000115");
		assertThat(instance.getIp()).isEqualTo("10.33.211.12");
		assertThat(instance.getPort()).isEqualTo(10000);
		assertThat(instance.getZnode()).isEqualTo("/beecomb/worker/instances/10.33.211.12:10000-0000000115");
		
		instance = ZooKeeperInstanceRegistry
				.resovleRegisteredInstance("/my/beecomb/worker/instances/10.33.211.12:10000-0000000115");
		assertThat(instance).isNotNull();
		assertThat(instance.getServiceName()).isEqualTo("worker");
		assertThat(instance.getInstanceName()).isEqualTo("10.33.211.12:10000-0000000115");
		assertThat(instance.getIp()).isEqualTo("10.33.211.12");
		assertThat(instance.getPort()).isEqualTo(10000);
		assertThat(instance.getZnode()).isEqualTo("/my/beecomb/worker/instances/10.33.211.12:10000-0000000115");
	}
}
