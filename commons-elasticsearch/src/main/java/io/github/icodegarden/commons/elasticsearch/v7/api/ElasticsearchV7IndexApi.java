package io.github.icodegarden.commons.elasticsearch.v7.api;

import java.io.IOException;
import java.util.Arrays;

import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchV7IndexApi {

	private final RestHighLevelClient client;

	public ElasticsearchV7IndexApi(RestHighLevelClient client) {
		this.client = client;
	}

	public void refreshIndices(String... indices) {
		RefreshRequest refreshRequest = new RefreshRequest(indices);
		try {
			RefreshResponse refreshResponse = client.indices().refresh(refreshRequest, RequestOptions.DEFAULT);
			if (refreshResponse.getFailedShards() > 0) {
				throw new IllegalStateException("refresh " + Arrays.asList(indices) + " failed, failed shards:"
						+ refreshResponse.getFailedShards());
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
