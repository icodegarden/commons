package com.geely.gddc.commons.dao.elasticsearch.v7;

import org.elasticsearch.client.RestHighLevelClient;

import com.geely.gddc.commons.dao.elasticsearch.DemoPO;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.elasticsearch.v7.dao.GenericElasticsearchV7Dao;

/**
 * 只需声明泛型的类型
 * @author Fangfang.Xu
 *
 */
public class DemoElasticsearchV7Dao
		extends GenericElasticsearchV7Dao<DemoPO, DemoPO, ElasticsearchQuery<Object>, Object, DemoPO> {

	public DemoElasticsearchV7Dao(RestHighLevelClient client) {
		super(client, "demo-index");
	}

}
