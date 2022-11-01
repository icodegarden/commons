package io.github.icodegarden.commons.mybatis.interceptor;

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
public class SqlPerformanceConfig {
	/**
	 * 超过这个毫秒数的属于非健康sql
	 */
	private long unhealthMs = 100;
	/**
	 * SQL 是否格式化
	 */
	private boolean format = true;
	/**
	 * 当使用sharding时是否只关注第一条sql，如分页查询时
	 */
//	private boolean firstSqlOnSharding = true;
}