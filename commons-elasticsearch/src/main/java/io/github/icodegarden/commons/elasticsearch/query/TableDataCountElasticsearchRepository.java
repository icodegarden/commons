package io.github.icodegarden.commons.elasticsearch.query;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.elasticsearch.repository.GenericElasticsearchRepository;
import io.github.icodegarden.commons.lang.query.TableDataCountPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class TableDataCountElasticsearchRepository extends
		GenericElasticsearchRepository<TableDataCountPO, TableDataCountPO, ElasticsearchQuery<Object>, Object, TableDataCountPO> {

	public TableDataCountElasticsearchRepository(ElasticsearchClient client, String index) {
		super(client, index);
	}

}
