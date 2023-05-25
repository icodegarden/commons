package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.hbase.HBaseEnv;
import io.github.icodegarden.commons.springboot.properties.CommonsHBaseProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(HBaseEnv.class)
@EnableConfigurationProperties({ CommonsHBaseProperties.class })
@Configuration
@Slf4j
public class CommonsHBaseAutoConfiguration {

	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "commons.hbase.client.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public HBaseEnv hbaseEnv(CommonsHBaseProperties commonsHBaseProperties) {
		log.info("commons init bean of HBaseEnv");
		
		commonsHBaseProperties.validate();

		Properties properties = new Properties();
		properties.setProperty("hbase.zookeeper.quorum", commonsHBaseProperties.getHbaseZookeeperQuorum());

		if (StringUtils.hasText(commonsHBaseProperties.getHbaseClientUsername())) {
			properties.setProperty("hbase.client.username", commonsHBaseProperties.getHbaseClientUsername());
		}
		if (StringUtils.hasText(commonsHBaseProperties.getHbaseClientPassword())) {
			properties.setProperty("hbase.client.password", commonsHBaseProperties.getHbaseClientPassword());
		}

		HBaseEnv hBaseEnv = new HBaseEnv(commonsHBaseProperties.getVersionFrom(), properties);
		hBaseEnv.setNamePrefix(commonsHBaseProperties.getNamePrefix());
		return hBaseEnv;
	}

}
