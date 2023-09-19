package io.github.icodegarden.commons.elasticsearch.v7.query;

import org.elasticsearch.client.RestHighLevelClient;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.elasticsearch.v7.repository.GenericElasticsearchV7Repository;
import io.github.icodegarden.commons.lang.query.TableDataCountPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class TableDataCountElasticsearchV7Repository extends
		GenericElasticsearchV7Repository<TableDataCountPO, TableDataCountPO, ElasticsearchQuery<Object>, Object, TableDataCountPO> {

	public TableDataCountElasticsearchV7Repository(RestHighLevelClient client, String index) {
		super(client, index);
	}

}
