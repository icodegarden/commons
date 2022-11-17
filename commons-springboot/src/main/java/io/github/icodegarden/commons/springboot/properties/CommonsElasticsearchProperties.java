package io.github.icodegarden.commons.springboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import io.github.icodegarden.commons.elasticsearch.ElasticsearchClientConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.elasticsearch")
@Getter
@Setter
@ToString(callSuper = true)
public class CommonsElasticsearchProperties extends ElasticsearchClientConfig {

}