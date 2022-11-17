package io.github.icodegarden.commons.elasticsearch.v7;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.NodeSelector;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.elasticsearch.ElasticsearchClientConfig;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class RestHighLevelClientBuilder {

	static RestHighLevelClient buildRestHighLevelClient(ElasticsearchClientConfig esProperties) {
		if (esProperties.getHttpHosts() == null) {
			throw new IllegalArgumentException("es httpHosts must be not empty");
		}

		try {
			String[] urls = esProperties.getHttpHosts().split(",");
			Node[] nodes = new Node[urls.length];
			for (int i = 0; i < urls.length; i++) {
				URI uri = new URI(urls[i]);
				nodes[i] = new Node(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));
			}

			RestClientBuilder builder = RestClient.builder(nodes);
			/**
			 * 在使用云ES时，提供的是一个域名负载均衡地址（就像一个单节点地址），似乎应该把云ES的地址认为是一直可用
			 */
//			builder.setFailureListener(new RestClient.FailureListener() {
//			    @Override
//			    public void onFailure(Node node) {
//			        log.error("node:{} was failed", node);
//			    }
//			});
			builder.setNodeSelector(NodeSelector.ANY); // default
			builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
				@Override
				public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
					return requestConfigBuilder.setConnectionRequestTimeout(esProperties.getConnectionRequestTimeout())
							.setConnectTimeout(esProperties.getConnectTimeout())
							.setSocketTimeout(esProperties.getSocketTimeout());
				}
			});
			builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
				@Override
				public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
					if (StringUtils.hasText(esProperties.getPassword())) {
						CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
						credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
								esProperties.getUsername(), esProperties.getPassword()));
						httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
					}
					httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
						@Override
						public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
							long keepAliveDuration = super.getKeepAliveDuration(response, context);
							if (keepAliveDuration < 0) {// < 0 无限，会跟服务端不一致报SocketTimeout
								return esProperties.getKeepAlive();
							}
							return keepAliveDuration;
						}
					});
//					httpClientBuilder.setConnectionManager(connManager)
//					httpClientBuilder.setConnectionReuseStrategy(reuseStrategy)
					httpClientBuilder.setMaxConnPerRoute(esProperties.getMaxConnPerRoute());
					httpClientBuilder.setMaxConnTotal(esProperties.getMaxConnTotal());
//					httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build());
					return httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom()
							.setIoThreadCount(Runtime.getRuntime().availableProcessors()/* default */).build());
				}
			});

			RestHighLevelClient client = new RestHighLevelClient(builder);
			return client;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}
}