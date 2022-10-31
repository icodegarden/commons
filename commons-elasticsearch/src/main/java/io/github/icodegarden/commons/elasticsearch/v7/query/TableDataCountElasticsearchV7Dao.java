package io.github.icodegarden.commons.elasticsearch.v7.query;

import org.elasticsearch.client.RestHighLevelClient;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.elasticsearch.v7.dao.GenericElasticsearchV7Dao;
import io.github.icodegarden.commons.lang.query.TableDataCountPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class TableDataCountElasticsearchV7Dao extends
		GenericElasticsearchV7Dao<TableDataCountPO, TableDataCountPO, ElasticsearchQuery<Object>, Object, TableDataCountPO> {

	public TableDataCountElasticsearchV7Dao(RestHighLevelClient client, String index) {
		super(client, index);
	}

}
