package io.github.icodegarden.commons.elasticsearch.query;

import java.util.List;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
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

	private TableDataCountElasticsearchDao tableDataCountElasticsearchDao;

	public ElasticsearchTableDataCountStorage(ElasticsearchClient client) {
		tableDataCountElasticsearchDao = new TableDataCountElasticsearchDao(client, TABLENAME);
	}

	@Override
	public void add(TableDataCountPO po) {
		po.setId(po.getTableName());// 表名作为id，否则自动生成
		tableDataCountElasticsearchDao.add(po);
	}

	@Override
	public int updateCount(String tableName, long count) {
		TableDataCountPO update = new TableDataCountPO();
		update.setId(tableName);
		update.setDataCount(count);
		update.setUpdatedAt(SystemUtils.now());
		return tableDataCountElasticsearchDao.update(update);
	}

	@Override
	public List<TableDataCountPO> findAll() {
		ElasticsearchQuery<Object> query = new ElasticsearchQuery<Object>();
		query.setSize(10000);
		return tableDataCountElasticsearchDao.findAll(query);
	}

	int delete(String id) {
		return tableDataCountElasticsearchDao.delete(id);
	}
}