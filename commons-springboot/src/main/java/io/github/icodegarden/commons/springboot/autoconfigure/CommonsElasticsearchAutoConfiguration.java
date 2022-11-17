package io.github.icodegarden.commons.springboot.autoconfigure;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.elasticsearch.ElasticsearchClientConfig;
import io.github.icodegarden.commons.springboot.properties.CommonsElasticsearchProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(ElasticsearchClientConfig.class)
@EnableConfigurationProperties({ CommonsElasticsearchProperties.class })
@Configuration
@Slf4j
public class CommonsElasticsearchAutoConfiguration {

	@ConditionalOnClass({ ElasticsearchClient.class })
	@Configuration
	protected static class ElasticsearchClientAutoConfiguration {

		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "commons.elasticsearch.client.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public ElasticsearchClient elasticsearchClient(CommonsElasticsearchProperties commonsElasticsearchProperties) {
			log.info("commons init bean of ElasticsearchClient");

			return commonsElasticsearchProperties.buildElasticsearchClient();
		}
	}

	@ConditionalOnClass({ RestHighLevelClient.class })
	@Configuration
	protected static class RestHighLevelClientAutoConfiguration {

		@ConditionalOnMissingBean
		@ConditionalOnProperty(value = "commons.elasticsearch.client.enabled", havingValue = "true", matchIfMissing = true)
		@Bean
		public RestHighLevelClient restHighLevelClient(CommonsElasticsearchProperties commonsElasticsearchProperties) {
			log.info("commons init bean of RestHighLevelClient");

			return commonsElasticsearchProperties.buildRestHighLevelClient();
		}
	}
}
