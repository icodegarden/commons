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
public class SqlConfig {
	/**
	 * 超过这个毫秒数的输出sql
	 */
	private long outputThresholdMs = 100;
	/**
	 * SQL 是否格式化
	 */
	private boolean format = true;
	/**
	 * 当使用sharding时是否只关注第一条sql，如分页查询时
	 */
//	private boolean firstSqlOnSharding = true;
}