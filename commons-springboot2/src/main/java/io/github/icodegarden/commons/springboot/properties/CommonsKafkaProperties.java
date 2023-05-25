package io.github.icodegarden.commons.springboot.properties;

import java.util.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.Validateable;
import lombok.Getter;
import lombok.NonNull;
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
public class CommonsKafkaProperties implements Validateable {

	@NonNull
	private String bootstrapServers;// 172.22.122.27:9092,172.22.122.28:9092
	@NonNull
	private Producer producer;

	@Override
	public void validate() throws IllegalArgumentException {
		Assert.hasText(bootstrapServers, "bootstrapServers must not empty");
		Assert.notNull(producer, "producer must not null");
	}

	@Getter
	@Setter
	@ToString
	public static class Producer implements Validateable {
		@NonNull
		private String keySerializer;// org.apache.kafka.common.serialization.StringSerializer
		@NonNull
		private String valueSerializer;// org.apache.kafka.common.serialization.StringSerializer

		/**
		 * 其他kafka props
		 */
		private Properties props = new Properties();

		@Override
		public void validate() throws IllegalArgumentException {
			Assert.hasText(keySerializer, "keySerializer must not empty");
			Assert.hasText(valueSerializer, "valueSerializer must not empty");
		}
	}

}