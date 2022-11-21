package io.github.icodegarden.commons.elasticsearch.api;

import java.io.IOException;
import java.util.Arrays;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.RefreshRequest;
import co.elastic.clients.elasticsearch.indices.RefreshResponse;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchIndexApi {

	private final ElasticsearchClient client;

	public ElasticsearchIndexApi(ElasticsearchClient client) {
		this.client = client;
	}

	public void refreshIndices(String... indices) {
		RefreshRequest request = new RefreshRequest.Builder().index(Arrays.asList(indices)).build();

		try {
			RefreshResponse refreshResponse = client.indices().refresh(request);
			if (refreshResponse.shards().failed().intValue() > 0) {
				throw new IllegalStateException("refresh " + Arrays.asList(indices) + " failed, failed shards:"
						+ refreshResponse.shards().failed().intValue());
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
