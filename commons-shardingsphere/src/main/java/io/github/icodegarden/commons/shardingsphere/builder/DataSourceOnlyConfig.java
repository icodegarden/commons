package io.github.icodegarden.commons.shardingsphere.builder;

import java.util.List;

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
public class DataSourceOnlyConfig {

	private List<DataSourceConfig> datasources;
	/**
	 * 可以多表用一个配置，如jobidrangemod
	 */
	private List<RangeModShardingAlgorithmConfig> rangeModShardingAlgorithms;

	/**
	 * 同组的一个String ,号间隔 如
	 * job_main,job_detail,delay_job,schedule_job,pending_recovery_job,job_execute_record,job_recovery_record
	 * 是一组
	 */
	private List<String> bindingTableGroups;
	/**
	 * 每个表一个String 如t_config
	 */
	private List<String> broadcastTables;
	/**
	 * 需要生成全局id的表，例如job_main
	 */
	private List<String> keyGeneratorTables;

	private List<ShardingTable> shardingTables;

}