package io.github.icodegarden.commons.elasticsearch.v7.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.client.RestHighLevelClient;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.lang.query.AbstractTableDataCountCollector;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchV7TableDataCountCollector extends AbstractTableDataCountCollector {

	private static final String TABLENAME = "table_data_count";

	private List<String> supportTables;

	private final RestHighLevelClient client;

	public ElasticsearchV7TableDataCountCollector(Set<String> supportTables, RestHighLevelClient client) {
		this.client = client;
		this.supportTables = new ArrayList<String>(supportTables);
	}

	@Override
	public String version() {
		return "7";
	}

	@Override
	public List<String> doListTables() {
		return supportTables;
	}

	@Override
	public long countTable(String tableName) {
		TableDataCountElasticsearchV7Repository repository = new TableDataCountElasticsearchV7Repository(client,
				TABLENAME);
		ElasticsearchQuery<Object> query = new ElasticsearchQuery<Object>();
		return repository.count(query);
	}

}