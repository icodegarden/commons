package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import io.github.icodegarden.beecomb.client.BeeCombClient;
import io.github.icodegarden.beecomb.client.UrlsBeeCombClient;
import io.github.icodegarden.beecomb.client.UrlsClientProperties;
import io.github.icodegarden.beecomb.client.ZooKeeperBeeCombClient;
import io.github.icodegarden.beecomb.client.ZooKeeperClientProperties;
import io.github.icodegarden.beecomb.client.security.Authentication;
import io.github.icodegarden.beecomb.client.security.BasicAuthentication;
import io.github.icodegarden.beecomb.common.properties.ZooKeeper;
import io.github.icodegarden.beecomb.executor.BeeCombExecutor;
import io.github.icodegarden.beecomb.executor.registry.JobHandler;
import io.github.icodegarden.commons.springboot.properties.CommonsBeeCombClientProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsBeeCombClientProperties.Master;
import io.github.icodegarden.commons.springboot.properties.CommonsBeeCombExecutorProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@Slf4j
public class CommonsBeecombAutoConfiguration {

	@ConditionalOnClass(BeeCombClient.class)
	@EnableConfigurationProperties({ CommonsBeeCombClientProperties.class})
	@Configuration
	protected class BeeCombClientAutoConfiguration {

		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "commons.beecomb.client.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public BeeCombClient beeCombClient(CommonsBeeCombClientProperties commonsBeeCombClientProperties) {
			log.info("commons init bean of BeeCombClient");

			CommonsBeeCombClientProperties.BasicAuthentication basicAuth = commonsBeeCombClientProperties
					.getBasicAuth();

			Authentication authentication = new BasicAuthentication(basicAuth.getUsername(), basicAuth.getPassword());

			ZooKeeper zkProps = commonsBeeCombClientProperties.getZookeeper();
			if (StringUtils.hasText(zkProps.getConnectString())) {
				ZooKeeper zooKeeper = new ZooKeeper();
				BeanUtils.copyProperties(zkProps, zooKeeper);

				ZooKeeperClientProperties clientProperties = new ZooKeeperClientProperties(authentication, zooKeeper);
				return new ZooKeeperBeeCombClient(clientProperties);
			}

			Master master = commonsBeeCombClientProperties.getMaster();
			if (StringUtils.hasText(master.getHttpHosts())) {
				List<String> httpHosts = Arrays.asList(master.getHttpHosts().split(","));
				UrlsClientProperties clientProperties = new UrlsClientProperties(authentication, httpHosts);
				return new UrlsBeeCombClient(clientProperties);
			}

			throw new IllegalArgumentException(
					"zooKeeper or master must config on init BeeCombClient, CommonsBeeCombClientProperties:"
							+ commonsBeeCombClientProperties);
		}

	}

	@ConditionalOnClass(BeeCombExecutor.class)
	@EnableConfigurationProperties({CommonsBeeCombExecutorProperties.class })
	@Configuration
	protected class BeecombExecutorAutoConfiguration {

		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "commons.beecomb.executor.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public BeeCombExecutor beeCombExecutor(CommonsBeeCombExecutorProperties properties,
				List<JobHandler> jobHandlers, Environment env) {
			log.info("commons init bean of BeeCombExecutor");

			String appName = env.getProperty("spring.application.name");
			Assert.hasText(appName, "spring.application.name must config");

			BeeCombExecutor beeCombExecutor = BeeCombExecutor.start(appName, properties);
			if (!CollectionUtils.isEmpty(jobHandlers)) {
				beeCombExecutor.registerReplace(jobHandlers);
			}

			return beeCombExecutor;
		}

	}
}
