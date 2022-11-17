package io.github.icodegarden.commons.elasticsearch.v7;

import org.elasticsearch.client.RestHighLevelClient;

import io.github.icodegarden.commons.elasticsearch.ElasticsearchClientConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchClientV7Builder {

	public static RestHighLevelClient buildRestHighLevelClient(ElasticsearchClientConfig esProperties) {
		ElasticsearchClientConfig.Sniffer snifferProps = esProperties.getSniffer();
		if (snifferProps.isEnabled()) {
			return SnifferRestHighLevelClientBuilder.buildRestHighLevelClient(esProperties);
		} else {
			return RestHighLevelClientBuilder.buildRestHighLevelClient(esProperties);
		}
	}
}