package com.geely.gddc.commons.dao.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.elasticsearch.dao.GenericElasticsearchDao;
import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DemoElasticsearchDao
		extends GenericElasticsearchDao<DemoPO, DemoPO, ElasticsearchQuery<Object>, Object, DemoPO> {

	public DemoElasticsearchDao(ElasticsearchClient client) {
		super(client, "demo-index");
	}

}
