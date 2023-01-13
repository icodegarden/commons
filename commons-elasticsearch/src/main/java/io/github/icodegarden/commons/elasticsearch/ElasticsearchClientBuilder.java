package io.github.icodegarden.commons.elasticsearch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ElasticsearchClientBuilder {

	public static ElasticsearchClient buildElasticsearchClient(ElasticsearchClientConfig esProperties) {
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
					if(esProperties.isKeepAlive()) {
						httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
							@Override
							public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
								long keepAliveDuration = super.getKeepAliveDuration(response, context);
								if (keepAliveDuration < 0) {// < 0 无限，会跟服务端不一致报SocketTimeout
									return esProperties.getKeepAliveSeconds();
								}
								return keepAliveDuration;
							}
						});
					}
					
//					httpClientBuilder.setConnectionManager(connManager)
//					httpClientBuilder.setConnectionReuseStrategy(reuseStrategy)
					httpClientBuilder.setMaxConnPerRoute(esProperties.getMaxConnPerRoute());
					httpClientBuilder.setMaxConnTotal(esProperties.getMaxConnTotal());
//					httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom().setIoThreadCount(1).build());
					return httpClientBuilder.setDefaultIOReactorConfig(IOReactorConfig.custom()
							.setIoThreadCount(Runtime.getRuntime().availableProcessors()/* default */).build());
				}
			});

			// Create the low-level client
			RestClient restClient = builder.build();

			// Create the transport with a Jackson mapper
			ElasticsearchTransport transport = new RestClientTransport(restClient,
					new JacksonJsonpMapper(newObjectMapper()));
			// And create the API client
			ElasticsearchClient client = new ElasticsearchClient(transport);

			return client;
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private static ObjectMapper newObjectMapper() {
		final DateTimeFormatter STANDARD_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		ObjectMapper om = new ObjectMapper();
		om = new ObjectMapper();
		om.setSerializationInclusion(Include.NON_NULL);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
					SerializerProvider serializerProvider) throws IOException {
				jsonGenerator.writeString(STANDARD_DATETIME_FORMATTER.format(localDateTime));
			}
		});
		timeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException, JsonProcessingException {
				String valueAsString = jsonParser.getValueAsString();
				return LocalDateTime.parse(valueAsString, STANDARD_DATETIME_FORMATTER);
			}
		});
		om.registerModule(timeModule);

		return om;
	}
}
