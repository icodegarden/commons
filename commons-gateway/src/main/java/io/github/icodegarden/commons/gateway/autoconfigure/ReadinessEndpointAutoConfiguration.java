package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.gateway.endpoint.ReadinessEndPoint;
import io.github.icodegarden.commons.gateway.endpoint.ReadinessEndpointWebExtension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnProperty(value = "commons.gateway.endpoint.readiness.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class ReadinessEndpointAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public ReadinessEndPoint readinessEndPoint() {
		return new ReadinessEndPoint();
	}

	@Bean
	@ConditionalOnBean(ReadinessEndPoint.class)
	@ConditionalOnMissingBean
	public ReadinessEndpointWebExtension readinessEndpointWebExtension(ReadinessEndPoint readinessEndPoint) {
		return new ReadinessEndpointWebExtension(readinessEndPoint);
	}

}
