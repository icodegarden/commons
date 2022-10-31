package io.github.icodegarden.commons.elasticsearch.v7.query;

import java.util.List;

import org.elasticsearch.client.RestHighLevelClient;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.lang.query.TableDataCountPO;
import io.github.icodegarden.commons.lang.query.TableDataCountStorage;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchTableDataCountStorage implements TableDataCountStorage {

	private static final String TABLENAME = "table_data_count";

	private TableDataCountElasticsearchV7Dao tableDataCountElasticsearchV7Dao;

	public ElasticsearchTableDataCountStorage(RestHighLevelClient client) {
		tableDataCountElasticsearchV7Dao = new TableDataCountElasticsearchV7Dao(client, TABLENAME);
	}

	@Override
	public void add(TableDataCountPO po) {
		po.setId(po.getTableName());// 表名作为id，否则自动生成
		tableDataCountElasticsearchV7Dao.add(po);
	}

	@Override
	public int updateCount(String tableName, long count) {
		TableDataCountPO update = new TableDataCountPO();
		update.setId(tableName);
		update.setDataCount(count);
		update.setUpdatedAt(SystemUtils.now());
		return tableDataCountElasticsearchV7Dao.update(update);
	}

	@Override
	public List<TableDataCountPO> findAll() {
		ElasticsearchQuery<Object> query = new ElasticsearchQuery<Object>();
		query.setSize(10000);
		return tableDataCountElasticsearchV7Dao.findAll(query);
	}

	int delete(String id) {
		return tableDataCountElasticsearchV7Dao.delete(id);
	}
}