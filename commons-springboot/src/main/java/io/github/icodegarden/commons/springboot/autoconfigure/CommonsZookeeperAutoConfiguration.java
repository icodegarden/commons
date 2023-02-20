package io.github.icodegarden.commons.springboot.autoconfigure;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.client.ZKClientConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.springboot.properties.CommonsZookeeperProperties;
import io.github.icodegarden.commons.zookeeper.ACLsSupplier;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(ZooKeeperHolder.class)
@ConditionalOnProperty(value = "commons.zookeeper.client.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ CommonsZookeeperProperties.class })
@Configuration
@Slf4j
public class CommonsZookeeperAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public ZooKeeperHolder zooKeeperHolder(CommonsZookeeperProperties commonsZookeeperProperties) {
		log.info("commons init bean of ZooKeeperHolder");

		commonsZookeeperProperties.validate();

		return new ZooKeeperHolder(commonsZookeeperProperties);
	}

	@ConditionalOnProperty(value = "commons.zookeeper.client.enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass(CuratorFramework.class)
	@Configuration
	public class CuratorFrameworkAutoConfiguration {

		@Bean
		public ACLsSupplier nullACLsSupplier() {
			return new ACLsSupplier.NullACLsSupplier();
		}

		@ConditionalOnMissingBean
		@Bean
		public CuratorFramework curatorFramework(CommonsZookeeperProperties commonsZookeeperProperties,
				ACLsSupplier aclsSupplier) {
			log.info("commons init bean of CuratorFramework");

			RetryPolicy retryPolicy = new RetryForever(3000);
			ZKClientConfig zkClientConfig = new ZKClientConfig();
			zkClientConfig.setProperty(ZKClientConfig.ZOOKEEPER_SERVER_PRINCIPAL,
					"zookeeper/" + commonsZookeeperProperties.getConnectString());
			CuratorFramework client = CuratorFrameworkFactory.newClient(commonsZookeeperProperties.getConnectString(),
					commonsZookeeperProperties.getSessionTimeout(), commonsZookeeperProperties.getConnectTimeout(),
					retryPolicy, zkClientConfig);

			if (!CollectionUtils.isEmpty(aclsSupplier.get())) {
				client.setACL().withACL(aclsSupplier.get());
			}

			client.start();
			return client;
		}
	}

}
