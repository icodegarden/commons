package io.github.icodegarden.commons.springboot.autoconfigure;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.lang.endpoint.GracefullyStartup;
import io.github.icodegarden.commons.mybatis.interceptor.SqlInterceptor;
import io.github.icodegarden.commons.mybatis.repository.MysqlMybatisDatabase;
import io.github.icodegarden.commons.springboot.MybatisGracefullyStartup;
import io.github.icodegarden.commons.springboot.properties.CommonsMybatisProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(SqlInterceptor.class)
@EnableConfigurationProperties({ CommonsMybatisProperties.class })
@Configuration
@Slf4j
public class CommonsMybatisAutoConfiguration {

	@Autowired
	private CommonsMybatisProperties mybatisProperties;

	@ConditionalOnProperty(value = "commons.mybatis.interceptor.sql.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public SqlInterceptor sqlInterceptor() {
		log.info("commons init bean of SqlInterceptor");

		SqlInterceptor sqlInterceptor = new SqlInterceptor();
		sqlInterceptor.setSqlConfig(mybatisProperties.getSql());
		sqlInterceptor.setSqlConsumer(sql -> {
			log.warn("{}ms Threshold sql: {}", mybatisProperties.getSql(), sql);
		});
		return sqlInterceptor;
	}

	@ConditionalOnClass(name = { "com.mysql.cj.MysqlConnection" })
	@ConditionalOnProperty(value = "commons.mybatis.gracefullystartup.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public GracefullyStartup mybatisGracefullyStartup(MysqlMybatisDatabase mysqlMybatisDatabase) {
		log.info("commons init bean of MybatisGracefullyStartup");

		return new MybatisGracefullyStartup(mysqlMybatisDatabase);
	}

	@ConditionalOnClass(MapperScan.class)
	@ConditionalOnProperty(value = "commons.mybatis.mapperScan.enabled", havingValue = "true", matchIfMissing = true)
	@MapperScan(basePackages = "${" + CommonsMybatisProperties.SCAN_BASE_PACKAGES + "}", basePackageClasses = {
			io.github.icodegarden.commons.mybatis.repository.MysqlMybatisDatabase.class, //
			io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisLockMapper.class,//
			io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisReadWriteLockMapper.class//
	})
	@Configuration
	protected static class MapperScanAutoConfiguration {
		{
			log.info("commons init bean of MapperScanAutoConfiguration");
		}
	}
}
