package io.github.icodegarden.commons.hbase.query;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import io.github.icodegarden.commons.lang.query.TableDataCountPO;
import io.github.icodegarden.commons.lang.query.TableDataCountStorage;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HBaseTableDataCountStorage implements TableDataCountStorage {

	protected static final byte[] FAMILY_NAME = "f".getBytes(Charset.forName("utf-8"));

	private static final String tableName = "table_data_count";

	// 创建 HBase连接，在程序生命周期内只需创建一次，该连接线程安全，可以共享给所有线程使用。
	// 在程序结束后，需要将Connection对象关闭，否则会造成连接泄露。
	// 也可以采用try finally方式防止泄露
	private final Connection connection;

	public HBaseTableDataCountStorage(Connection connection) {
		this.connection = connection;
	}

	@Override
	public void add(TableDataCountPO po) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			/**
			 * 表名作为主键
			 */
			Put put = new Put(Bytes.toBytes(po.getTableName()));

			put.addColumn(FAMILY_NAME, Bytes.toBytes("data_count"), Bytes.toBytes(po.getDataCount()));
			put.addColumn(FAMILY_NAME, Bytes.toBytes("updated_at"),
					Bytes.toBytes(SystemUtils.STANDARD_DATETIME_FORMATTER.format(po.getUpdatedAt())));

			table.put(put);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public int updateCount(String tableName, long count) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			/**
			 * 表名作为主键
			 */
			Put put = new Put(Bytes.toBytes(tableName));

			put.addColumn(FAMILY_NAME, Bytes.toBytes("data_count"), Bytes.toBytes(count));
			put.addColumn(FAMILY_NAME, Bytes.toBytes("updated_at"),
					Bytes.toBytes(SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now())));

			table.put(put);
			return 1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public List<TableDataCountPO> findAll() {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			Scan scan = new Scan();
			scan.setCaching(1000);// default 100

			scan.addColumn(FAMILY_NAME, Bytes.toBytes("data_count"));
			scan.addColumn(FAMILY_NAME, Bytes.toBytes("updated_at"));

			ResultScanner scanner = table.getScanner(scan);
			List<TableDataCountPO> list = new LinkedList<>();
			for (Result result : scanner) {
				TableDataCountPO po = new TableDataCountPO();
				/**
				 * 表名作为主键
				 */
				po.setId(Bytes.toString(result.getRow()));
				po.setTableName(po.getId());

				byte[] data_count = result.getValue(FAMILY_NAME, Bytes.toBytes("data_count"));
				if (data_count != null) {
					po.setDataCount(Bytes.toLong(data_count));
				}
				byte[] updated_at = result.getValue(FAMILY_NAME, Bytes.toBytes("updated_at"));
				if (updated_at != null) {
					po.setUpdatedAt(
							LocalDateTime.parse(Bytes.toString(updated_at), SystemUtils.STANDARD_DATETIME_FORMATTER));
				}

				list.add(po);
			}
			scanner.close();

			return list;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	int delete(String id) {
		// Table 为非线程安全对象，每个线程在对Table操作时，都必须从Connection中获取相应的Table对象
		try (Table table = connection.getTable(TableName.valueOf(tableName))) {
			/**
			 * 表名作为主键
			 */
			Delete delete = new Delete(Bytes.toBytes(id));
			table.delete(delete);
			return 1;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}