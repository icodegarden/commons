package io.github.icodegarden.commons.shardingsphere.builder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class DataSourceConfig {

	/**
	 * datasource name 如ds0
	 */
	private String name;
	private String jdbcUrl;
	private String username;
	private String password;
	/**
	 * 
	 */
	private Integer minimumIdle;
	private Long idleTimeout;
	private Integer maximumPoolSize;
	private Long maxLifetime;
	private Long connectionTimeout;
	private String connectionTestQuery;
	private Long keepaliveTime;
	private Long validationTimeout;

}