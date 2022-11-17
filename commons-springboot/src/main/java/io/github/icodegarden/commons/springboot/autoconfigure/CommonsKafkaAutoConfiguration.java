package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.kafka.reliability.ReliabilityProducer;
import io.github.icodegarden.commons.springboot.properties.CommonsKafkaProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsKafkaProperties.Producer;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(ReliabilityProducer.class)
@EnableConfigurationProperties({ CommonsKafkaProperties.class })
@Configuration
@Slf4j
public class CommonsKafkaAutoConfiguration {

	@ConditionalOnMissingBean
	@ConditionalOnProperty(value = "commons.kafka.reliability.producer.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public ReliabilityProducer<String, String> reliabilityProducer(CommonsKafkaProperties commonsKafkaProperties) {
		log.info("commons init bean of ReliabilityProducer");

		Producer producer = commonsKafkaProperties.getProducer();

		Properties props = new Properties();
		props.put("bootstrap.servers", commonsKafkaProperties.getBootstrapServers());
		props.put("key.serializer", producer.getKeySerializer());
		props.put("value.serializer", producer.getValueSerializer());
		props.putAll(producer.getProps());
		return new ReliabilityProducer<>(props);
	}

}
