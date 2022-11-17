package io.github.icodegarden.commons.springboot.properties;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConfigurationProperties(prefix = "commons.kafka")
@Getter
@Setter
@ToString
public class CommonsKafkaProperties {

	private String bootstrapServers;// 172.22.122.27:9092,172.22.122.28:9092
	
	private Producer producer;

	@Getter
	@Setter
	@ToString
	public static class Producer {
		private String keySerializer;// org.apache.kafka.common.serialization.StringSerializer
		private String valueSerializer;// org.apache.kafka.common.serialization.StringSerializer

		/**
		 * 其他kafka props
		 */
		private Properties props = new Properties();
	}
}