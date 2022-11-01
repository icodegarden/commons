package io.github.icodegarden.commons.elasticsearch.query;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.elasticsearch.dao.GenericElasticsearchDao;
import io.github.icodegarden.commons.lang.query.TableDataCountPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class TableDataCountElasticsearchDao extends
		GenericElasticsearchDao<TableDataCountPO, TableDataCountPO, ElasticsearchQuery<Object>, Object, TableDataCountPO> {

	public TableDataCountElasticsearchDao(ElasticsearchClient client, String index) {
		super(client, index);
	}

}
