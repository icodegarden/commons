package io.github.icodegarden.commons.hbase.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;

import io.github.icodegarden.commons.lang.query.AbstractTableDataCountCollector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HBaseTableDataCountCollector extends AbstractTableDataCountCollector {

	private String hbaseVersion;
	private List<String> supportTables;

	// 创建 HBase连接，在程序生命周期内只需创建一次，该连接线程安全，可以共享给所有线程使用。
	// 在程序结束后，需要将Connection对象关闭，否则会造成连接泄露。
	// 也可以采用try finally方式防止泄露
	private final Connection connection;
	private final AggregationClient aggregationClient;

	public HBaseTableDataCountCollector(Set<String> supportTables, Connection connection,
			AggregationClient aggregationClient) {
		this.connection = connection;
		this.aggregationClient = aggregationClient;
		this.supportTables = new ArrayList<String>(supportTables);
	}

//	public HBaseTableDataCountCollector(Set<String> supportTables, Connection connection,
//			AggregationClient aggregationClient, Set<String> whiteListTables) {
//		super(whiteListTables);
//		this.connection = connection;
//		this.aggregationClient = aggregationClient;
//		this.supportTables = new ArrayList<String>(supportTables);
//	}

	@Override
	public String version() {
		return hbaseVersion;
	}

	@Override
	public List<String> doListTables() {
		return supportTables;
	}

	@Override
	public long countTable(String tableName) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			Scan scan = new Scan();
			scan.setCaching(1000);// default 100

			return aggregationClient.rowCount(table, new LongColumnInterpreter(), scan);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}

}