package io.github.icodegarden.commons.zookeeper.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.metrics.Metrics;
import io.github.icodegarden.commons.lang.metrics.Metrics.Dimension;
import io.github.icodegarden.commons.lang.metrics.Metrics.DimensionName;
import io.github.icodegarden.commons.zookeeper.CommonZookeeperBuilder;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperInstanceRegistry;
import io.github.icodegarden.commons.zookeeper.registry.ZooKeeperRegisteredInstance;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ZnodeDataZooKeeperInstanceMetricsTests extends CommonZookeeperBuilder {

	String root = "/xff";
	int port = 9999;
	ZooKeeperInstanceRegistry zooKeeperInstanceRegister;
	ZooKeeperInstanceMetrics zooKeeperInstanceMetrics;

	@BeforeEach
	void init() {
		zooKeeperInstanceRegister = new ZooKeeperInstanceRegistry(zkh, root, "worker", port);
		zooKeeperInstanceMetrics = new ZnodeDataZooKeeperInstanceMetrics(zkh, root);
	}

	@Test
	void setMetrics() throws Exception {
		ZooKeeperRegisteredInstance result = zooKeeperInstanceRegister.registerIfNot();

		Metrics metrics = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Jobs, 4, 2)));
		zooKeeperInstanceMetrics.setMetrics(result, metrics);

		zooKeeperInstanceRegister.deregister();
		zooKeeperInstanceMetrics.removeMetrics(result);
	}
	
	@Test
	void getMetrics() throws Exception {
		ZooKeeperRegisteredInstance result = zooKeeperInstanceRegister.registerIfNot();
		
		Metrics metrics = zooKeeperInstanceMetrics.getMetrics(result);
		assertThat(metrics).isNull();
		
		//--------------------------------------
		metrics = new Metrics(Arrays.asList(new Dimension(Metrics.DimensionName.Jobs, 4, 2)));
		zooKeeperInstanceMetrics.setMetrics(result, metrics);
		metrics = zooKeeperInstanceMetrics.getMetrics(result);
		assertThat(metrics).isNotNull();
		assertThat(metrics.getDimension(DimensionName.Jobs).getMax()).isEqualTo(4);
		assertThat(metrics.getDimension(DimensionName.Jobs).getUsed()).isEqualTo(2);
		
		//--------------------------------------
		zooKeeperInstanceMetrics.removeMetrics(result);
		metrics = zooKeeperInstanceMetrics.getMetrics(result);
		assertThat(metrics).isNull();
	}

	@Test
	void listMetrics() throws Exception {
		//????????????----------------------------------
		List<Metrics> listMetrics = zooKeeperInstanceMetrics.listMetrics("worker");
		assertThat(listMetrics).isEmpty();

		//?????????????????????????????????----------------------------------
		ZooKeeperRegisteredInstance result = zooKeeperInstanceRegister.registerIfNot();
		listMetrics = zooKeeperInstanceMetrics.listMetrics("worker");
		assertThat(listMetrics).isEmpty();

		//???????????????
		Metrics metrics = new Metrics(Arrays.asList(new Dimension(DimensionName.Jobs, 4, 0)));
		zooKeeperInstanceMetrics.setMetrics(result, metrics);
		listMetrics = zooKeeperInstanceMetrics.listMetrics("worker");
		assertThat(listMetrics).hasSize(1);
		assertThat(listMetrics.get(0).getDimension(DimensionName.Jobs).getUsed()).isEqualTo(0);
		assertThat(listMetrics.get(0).getInstanceName()).isEqualTo(result.getInstanceName());

		//????????????
		metrics.setDimension(new Dimension(Metrics.DimensionName.Jobs, 4, 2));
		zooKeeperInstanceMetrics.setMetrics(result, metrics);
		listMetrics = zooKeeperInstanceMetrics.listMetrics("worker");
		assertThat(listMetrics.get(0).getDimension(DimensionName.Jobs).getUsed()).isEqualTo(2);
		assertThat(listMetrics.get(0).getInstanceName()).isEqualTo(result.getInstanceName());
	}

}
