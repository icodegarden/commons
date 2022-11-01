package io.github.icodegarden.commons.springboot.configuration;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.mybatis.interceptor.SqlPerformanceInterceptor;
import io.github.icodegarden.commons.springboot.properties.CommonsMybatisProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(Mapper.class)
@EnableConfigurationProperties({ CommonsMybatisProperties.class })
@Configuration
@Slf4j
public class CommonsMybatisAutoConfiguration {

	@ConditionalOnClass(MapperScan.class)
	@ConditionalOnProperty(value = "commons.mybatis.mapperScan.enabled", havingValue = "true", matchIfMissing = true)
	@MapperScan(basePackages = "${" + CommonsMybatisProperties.SCAN_BASE_PACKAGES + "}")
	@Configuration
	protected static class MapperScanAutoConfiguration {
	}

	@ConditionalOnClass(SqlPerformanceInterceptor.class)
	@ConditionalOnProperty(value = "commons.mybatis.interceptor.sqlPerformance.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	protected static class SqlPerformanceInterceptorAutoConfiguration {
		@Autowired
		private CommonsMybatisProperties mybatisProperties;

		@Bean
		public SqlPerformanceInterceptor sqlPerformanceInterceptor() {
			SqlPerformanceInterceptor sqlPerformanceInterceptor = new SqlPerformanceInterceptor();
			sqlPerformanceInterceptor.setSqlPerformanceConfig(mybatisProperties.getSql());
			sqlPerformanceInterceptor.setUnhealthSqlConsumer(sql -> {
				log.warn("unhealth sql : {}", sql);
			});
			return sqlPerformanceInterceptor;
		}
	}

}
