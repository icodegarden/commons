package io.github.icodegarden.commons.springboot.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.csp.sentinel.SphU;

import io.github.icodegarden.commons.springboot.configuration.CommonsSentinelProperties.Cluster;
import io.github.icodegarden.commons.springboot.configuration.CommonsSentinelProperties.Nacos;
import io.github.icodegarden.commons.springboot.sentinel.SentinelClusterClientStarter;
import io.github.icodegarden.commons.springboot.sentinel.SentinelEventStarter;
import io.github.icodegarden.commons.springboot.sentinel.SentinelNacosDynamicRuleStarter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass({ SphU.class })
@EnableConfigurationProperties({ CommonsSentinelProperties.class })
@Configuration
@Slf4j
public class CommonsSentinelAutoConfiguration {

	@Autowired
	private Environment env;
	@Autowired
	private CommonsSentinelProperties sentinelProperties;

	@PostConstruct
	private void init() {
		if (!ClassUtils.isPresent("com.alibaba.cloud.sentinel.SentinelProperties", null)) {
			/**
			 * 当没有依赖spring-cloud-starter-alibaba-sentinel，要在sentinel初始化前设置-D参数地址，而不能通过application.yml自动生效
			 */
			String dashboardAddr = env.getProperty("spring.cloud.sentinel.transport.dashboard");
			log.info(
					"this project maven dependency not contains spring-cloud-starter-alibaba-sentinel, will set sentinel dashboard:{}",
					dashboardAddr);
			Assert.hasText(dashboardAddr,
					"dashboardAddr must not empty when dependency not contains spring-cloud-starter-alibaba-sentinel");
			System.setProperty("csp.sentinel.dashboard.server", dashboardAddr);
		}

		SentinelEventStarter.addDefaultLoggingObserver();

		Cluster cluster = sentinelProperties.getCluster();
		log.info("sentinel cluster is enbaled:{}", cluster.getEnabled());
		if (cluster.getEnabled()) {
			SentinelClusterClientStarter.start(cluster.getServerAddr(), cluster.getServerPort());
		}
	}

	@ConditionalOnBean(NacosConfigProperties.class)
	@ConditionalOnClass({ SphU.class })
	@Configuration
	protected static class DynamicRuleAutoConfiguration {
		@Autowired
		private Environment env;
		@Autowired
		private NacosConfigProperties nacosConfigProperties;
		@Autowired
		private CommonsSentinelProperties sentinelProperties;

		@PostConstruct
		private void init() {
			String dataId = env.getRequiredProperty("spring.application.name");
			Nacos nacos = sentinelProperties.getNacos();

			SentinelNacosDynamicRuleStarter.start(nacosConfigProperties, dataId, nacos.getGroupId());
		}
	}
}