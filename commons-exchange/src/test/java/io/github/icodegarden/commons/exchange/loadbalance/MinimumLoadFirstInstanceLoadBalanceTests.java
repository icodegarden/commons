package io.github.icodegarden.commons.exchange.loadbalance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.metrics.InstanceMetrics;
import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.Dimension;
import io.github.icodegarden.commons.lang.registry.InstanceDiscovery;
import io.github.icodegarden.commons.lang.registry.RegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MinimumLoadFirstInstanceLoadBalanceTests {

	/**
	 * 推荐的使用方式，配合度量
	 * @throws Exception
	 */
	@Test
	void selectCandidates() throws Exception {
		InstanceDiscovery<RegisteredInstance> instanceDiscovery = mock(InstanceDiscovery.class);
		InstanceMetrics<Metrics> instanceMetrics = mock(InstanceMetrics.class);
		
		/**
		 * 
		 */
		RegisteredInstance r1 = new RegisteredInstance.Default("worker", "r1", "1.1.1.1", 9999);
		RegisteredInstance r2 = new RegisteredInstance.Default("worker", "r2", "1.1.1.1", 9999);
		RegisteredInstance r3 = new RegisteredInstance.Default("worker", "r3", "1.1.1.1", 9999);
		RegisteredInstance r4 = new RegisteredInstance.Default("worker", "r4", "1.1.1.1", 9999);
		List<RegisteredInstance> instances = Arrays.asList(r1, r2, r3, r4);
		doReturn(instances).when(instanceDiscovery).listInstances(any());
		/**
		 * 
		 */
		Metrics m1 = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Cpu, 2, 1),
				new Dimension(Metrics.DimensionName.Memory, 2048, 1024),
				new Dimension(Metrics.DimensionName.Jobs, 200, 100)));
		m1.setInstanceName(r1.getInstanceName());
		Metrics m2 = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Cpu, 4, 1),
				new Dimension(Metrics.DimensionName.Memory, 4096, 1024),
				new Dimension(Metrics.DimensionName.Jobs, 400, 100)));
		m2.setInstanceName(r2.getInstanceName());
		Metrics m3 = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Cpu, 8, 1),
				new Dimension(Metrics.DimensionName.Memory, 8192, 2048),
				new Dimension(Metrics.DimensionName.Jobs, 800, 200)));
		m3.setInstanceName(r3.getInstanceName());
		Metrics m4 = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Jobs, 200, 200)));// 满了
		m4.setInstanceName(r4.getInstanceName());
		List<Metrics> metrics = Arrays.asList(m1, m2, m3, m4);
		doReturn(metrics).when(instanceMetrics).listMetrics(any());

		MinimumLoadFirstInstanceLoadBalance minimumLoadInstanceLoadBalance = new MinimumLoadFirstInstanceLoadBalance(
				instanceDiscovery, instanceMetrics);
		
		
		Queue<MetricsInstance> candidates = minimumLoadInstanceLoadBalance.selectCandidates("worker", 4);
		assertThat(candidates).hasSize(4);
		assertThat(candidates.poll().getAvailable()).isEqualTo(r3);
		assertThat(candidates.poll().getAvailable()).isEqualTo(r2);
		assertThat(candidates.poll().getAvailable()).isEqualTo(r1);
		assertThat(candidates.poll().getAvailable()).isNull();// 负载满了，不可用
		assertThat(candidates.poll()).isNull();

		candidates = minimumLoadInstanceLoadBalance.selectCandidates("worker", 1);
		assertThat(candidates).hasSize(1);
		MetricsInstance metricsInstance = candidates.poll();
		assertThat(metricsInstance.getAvailable()).isEqualTo(r3);
		assertThat(metricsInstance.getMetrics() == m3).isTrue();//要求返回的Metrics对象与InstanceMetrics的是相同的引用，这在master JobDispatcher中需要
		assertThat(candidates.poll()).isNull();
	}
	
	/**
	 * 允许没有度量
	 * @throws Exception
	 */
	@Test
	void selectCandidates_allowNullMetrics() throws Exception {
		InstanceDiscovery<RegisteredInstance> instanceDiscovery = mock(InstanceDiscovery.class);
		InstanceMetrics<Metrics> instanceMetrics = mock(InstanceMetrics.class);
		
		/**
		 * 
		 */
		RegisteredInstance r1 = new RegisteredInstance.Default("worker", "r1", "1.1.1.1", 9999);
		RegisteredInstance r2 = new RegisteredInstance.Default("worker", "r2", "1.1.1.1", 9999);
		RegisteredInstance r3 = new RegisteredInstance.Default("worker", "r3", "1.1.1.1", 9999);
		RegisteredInstance r4 = new RegisteredInstance.Default("worker", "r4", "1.1.1.1", 9999);
		List<RegisteredInstance> instances = Arrays.asList(r1, r2, r3, r4);
		doReturn(instances).when(instanceDiscovery).listInstances(any());

		MinimumLoadFirstInstanceLoadBalance minimumLoadInstanceLoadBalance = new MinimumLoadFirstInstanceLoadBalance(
				instanceDiscovery, instanceMetrics);
		
		Queue<MetricsInstance> candidates = minimumLoadInstanceLoadBalance.selectCandidates("worker", 4);
		assertThat(candidates).hasSize(4);
	}
	/**
	 * 不允许没有度量
	 * @throws Exception
	 */
	@Test
	void selectCandidates_notAllowNullMetrics() throws Exception {
		InstanceDiscovery<RegisteredInstance> instanceDiscovery = mock(InstanceDiscovery.class);
		InstanceMetrics<Metrics> instanceMetrics = mock(InstanceMetrics.class);
		
		/**
		 * 
		 */
		RegisteredInstance r1 = new RegisteredInstance.Default("worker", "r1", "1.1.1.1", 9999);
		RegisteredInstance r2 = new RegisteredInstance.Default("worker", "r2", "1.1.1.1", 9999);
		RegisteredInstance r3 = new RegisteredInstance.Default("worker", "r3", "1.1.1.1", 9999);
		RegisteredInstance r4 = new RegisteredInstance.Default("worker", "r4", "1.1.1.1", 9999);
		List<RegisteredInstance> instances = Arrays.asList(r1, r2, r3, r4);
		doReturn(instances).when(instanceDiscovery).listInstances(any());

		MinimumLoadFirstInstanceLoadBalance minimumLoadInstanceLoadBalance = new MinimumLoadFirstInstanceLoadBalance(
				instanceDiscovery, instanceMetrics);
		minimumLoadInstanceLoadBalance.setAllowNullMetrics(false);
		
		Queue<MetricsInstance> candidates = minimumLoadInstanceLoadBalance.selectCandidates("worker", 4);
		assertThat(candidates).hasSize(0);
	}
}
