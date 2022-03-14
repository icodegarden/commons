package io.github.icodegarden.commons.lang.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.registry.FilterableInstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class FilterableInstanceDiscoveryTests {

	@Test
	void listInstances() throws Exception {
		InstanceDiscovery instanceDiscovery = mock(InstanceDiscovery.class);
		
		FilterableInstanceDiscovery filterableInstanceDiscovery = new FilterableInstanceDiscovery<RegisteredInstance>(instance -> {
			return instance.getIp().equals("1.1.1.1");
		}, instanceDiscovery);
		
		// 模拟还未注册，没有实例-------------------------
		RegisteredInstance registeredInstance1 = new RegisteredInstance.Default("worker", "worker1", "1.1.1.1", 10000);
		RegisteredInstance registeredInstance2 = new RegisteredInstance.Default("worker", "worker2", "1.1.1.2", 10000);
		doReturn(Arrays.asList(registeredInstance1,registeredInstance2)).when(instanceDiscovery).listInstances(eq("worker"));
		List<RegisteredInstance> workers = filterableInstanceDiscovery.listInstances("worker");
		assertThat(workers).hasSize(1);
		assertThat(workers.get(0).getInstanceName()).isEqualTo("worker1");
	}

}
