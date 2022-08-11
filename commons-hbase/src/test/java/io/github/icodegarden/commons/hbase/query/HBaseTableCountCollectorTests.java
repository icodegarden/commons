package io.github.icodegarden.commons.hbase.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class HBaseTableCountCollectorTests {

	/**
	 * 使用devAliyun必须使用aliyun hbase客户端，否则阻塞不动
	 */
	HBaseEnv hBaseEnv = HBaseEnv.devAliyun();
	Connection connection = hBaseEnv.getConnection();
	AggregationClient aggregationClient = hBaseEnv.getAggregationClient();

	HBaseTableDataCountCollector collector = new HBaseTableDataCountCollector(
			new HashSet<String>(Arrays.asList("dev_data_main")), connection, aggregationClient);

	@Test
	void version() throws Exception {
		
	}

	@Test
	void listTables() throws Exception {
		List<String> listTables = collector.listTables();

		Assertions.assertThat(listTables).isNotEmpty();
	}

	@Test
	void countTable() throws Exception {
		/**
		 * 需要先人工建表
		 */
		long countTable = collector.countTable("dev_data_main");

		Assertions.assertThat(countTable).isGreaterThan(0);
	}
}
