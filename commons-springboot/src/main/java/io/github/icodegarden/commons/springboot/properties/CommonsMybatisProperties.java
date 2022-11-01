package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.mybatis.interceptor.SqlPerformanceConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.mybatis")
@Getter
@Setter
@ToString(callSuper = true)
public class CommonsMybatisProperties {

	public static final String SCAN_BASE_PACKAGES = "commons.mybatis.mapperScan.basePackages";

	private SqlPerformanceConfig sql = new SqlPerformanceConfig();

}