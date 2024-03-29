package io.github.icodegarden.commons.springboot.autoconfigure;

import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.springboot.endpoint.ReadinessEndpoint;
import io.github.icodegarden.commons.springboot.endpoint.ReadinessEndpointEnabledConditions;
import io.github.icodegarden.commons.springboot.endpoint.ReadinessEndpointWebExtension;
import io.github.icodegarden.commons.springboot.endpoint.ReadinessHealthIndicator;
import io.github.icodegarden.commons.springboot.properties.CommonsEndpointProperties;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EnableConfigurationProperties(CommonsEndpointProperties.class)
@Configuration
public class CommonsEndpointAutoConfiguration {

	/**
	 * 默认只针对网关
	 */
	@Conditional(ReadinessEndpointEnabledConditions.class)
	@ConditionalOnProperty(value = "commons.endpoint.readiness.enabled", havingValue = "true", matchIfMissing = true)
	@Configuration
	public class ReadinessEndpointAutoConfiguration {

		@ConditionalOnMissingBean
		@Bean
		public ReadinessEndpoint readinessEndpoint() {
			ReadinessEndpoint readinessEndpoint = new ReadinessEndpoint();

			return readinessEndpoint;
		}

		@Bean
		@ConditionalOnBean(ReadinessEndpoint.class)
		@ConditionalOnMissingBean
		public ReadinessEndpointWebExtension readinessEndpointWebExtension(ReadinessEndpoint readinessEndpoint) {
			return new ReadinessEndpointWebExtension(readinessEndpoint);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("readiness")
		public ReadinessHealthIndicator readinessHealthIndicator(ReadinessEndpoint readinessEndpoint) {
			return new ReadinessHealthIndicator(readinessEndpoint);
		}
	}
}
