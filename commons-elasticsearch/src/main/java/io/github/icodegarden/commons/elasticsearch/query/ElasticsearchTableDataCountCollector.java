package io.github.icodegarden.commons.elasticsearch.query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import io.github.icodegarden.commons.lang.query.AbstractTableDataCountCollector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchTableDataCountCollector extends AbstractTableDataCountCollector {

	private List<String> supportTables;

	private final ElasticsearchClient client;

	public ElasticsearchTableDataCountCollector(Set<String> supportTables, ElasticsearchClient client) {
		this.client = client;
		this.supportTables = new ArrayList<String>(supportTables);
	}

	@Override
	public String version() {
		return "8+";
	}

	@Override
	public List<String> doListTables() {
		return supportTables;
	}

	@Override
	public long countTable(String tableName) {
		CountRequest.Builder builder = new CountRequest.Builder().index(tableName);

		co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder queryBuilder = new co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder();
		builder.query(queryBuilder.build());
		try {
			CountResponse countResponse = client.count(builder.build());
			return countResponse.count();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}