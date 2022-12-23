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
public class ShardingTable {
	/**
	 * 例如 job_main
	 */
	private String tableName;
	/**
	 * 例如 id
	 */
	private String shardingColumn;
	/**
	 * 对应RangeModShardingAlgorithmProperties.algorithmName
	 */
	private String shardingAlgorithmName;
	/**
	 * 需要生成全局id的列名 例如id， 不配置表示不需要生成（例如job_detail）
	 */
	private String keyGenerateColumn;
}