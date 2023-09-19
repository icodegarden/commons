package io.github.icodegarden.commons.elasticsearch.repository;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.elasticsearch.repository.GenericElasticsearchRepository;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DemoElasticsearchRepository
		extends GenericElasticsearchRepository<DemoPO, DemoPO, ElasticsearchQuery<Object>, Object, DemoPO> {

	public DemoElasticsearchRepository(ElasticsearchClient client) {
		super(client, "demo-index");
	}

}
