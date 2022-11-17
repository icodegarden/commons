package io.github.icodegarden.commons.springboot.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.springboot.properties.CommonsZookeeperProperties;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(ZooKeeperHolder.class)
@EnableConfigurationProperties({ CommonsZookeeperProperties.class })
@Configuration
@Slf4j
public class CommonsZookeeperAutoConfiguration {

	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "commons.zookeeper.client.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public ZooKeeperHolder zooKeeperHolder(CommonsZookeeperProperties commonsZookeeperProperties) {
		log.info("commons init bean of ZooKeeperHolder");

		commonsZookeeperProperties.validate();
		
		return new ZooKeeperHolder(commonsZookeeperProperties);
	}

}
