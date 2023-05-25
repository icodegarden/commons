package io.github.icodegarden.commons.springboot.sentinel;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientAssignConfig;
import com.alibaba.csp.sentinel.cluster.client.config.ClusterClientConfigManager;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class SentinelClusterClientStarter {

	/**
	 * 适用于依赖了spring-cloud-starter-alibaba-sentinel
	 */
	public static void start(String serverHost, Integer serverPort) {
		start(serverHost, serverPort, null);
	}

	/**
	 * 当没有依赖spring-cloud-starter-alibaba-sentinel时，必须有projectName
	 */
	public static void start(String serverHost, Integer serverPort, String projectName) {
		/**
		 * 包含则说明有maven依赖
		 */
		if (!ClassUtils.isPresent("com.alibaba.cloud.sentinel.SentinelProperties", null)) {
			/**
			 * 当没有依赖spring-cloud-starter-alibaba-sentinel，不会自动设置project.name需要自行设置，这关系到client和server之间的namespace对应，否则会导致server那边对不上而无法连上server
			 */
			log.info(
					"this project maven dependency not contains spring-cloud-starter-alibaba-sentinel, will set sentinel project.name:{}",
					projectName);
			Assert.hasText(projectName,
					"projectName must not empty when dependency not contains spring-cloud-starter-alibaba-sentinel");
			System.setProperty("project.name", projectName);
//			System.setProperty("server.port", "8719");//这个会默认，冲突会自增
		}

		log.info("starting sentinel cluster of client, serverHost:{}, serverPort:{}", serverHost, serverPort);
		ClusterStateManager.applyState(ClusterStateManager.CLUSTER_CLIENT);

		DynamicSentinelProperty<ClusterClientAssignConfig> sentinelProperty = new DynamicSentinelProperty<ClusterClientAssignConfig>();
		ClusterClientConfigManager.registerServerAssignProperty(sentinelProperty);
		ClusterClientAssignConfig clusterClientAssignConfig = new ClusterClientAssignConfig(serverHost, serverPort);
		sentinelProperty.updateValue(clusterClientAssignConfig);// 触发一下

		/**
		 * 即使没有依赖spring-cloud-starter-alibaba-sentinel， sentinel client也会自动启动
		 */
//		DefaultClusterTokenClient tokenClient = new DefaultClusterTokenClient();
//		try {
//			tokenClient.start();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}