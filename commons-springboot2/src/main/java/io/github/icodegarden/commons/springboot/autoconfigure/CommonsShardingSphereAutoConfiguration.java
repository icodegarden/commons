package io.github.icodegarden.commons.springboot.autoconfigure;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.shardingsphere.builder.DataSourceOnlyApiShardingSphereBuilder;
import io.github.icodegarden.commons.springboot.properties.CommonsShardingSphereProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnClass({ ShardingSphereDataSource.class })
@EnableConfigurationProperties({ CommonsShardingSphereProperties.class })
@Configuration
@Slf4j
public class CommonsShardingSphereAutoConfiguration {

	{
		log.info("commons init bean of CommonsShardingSphereAutoConfiguration");
	}

	/**
	 * sharding DataSource
	 */
	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "commons.shardingsphere.datasource.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public DataSource dataSource(CommonsShardingSphereProperties properties) throws SQLException {
		DataSource dataSource = DataSourceOnlyApiShardingSphereBuilder.getDataSource(properties);
		return dataSource;
	}
}