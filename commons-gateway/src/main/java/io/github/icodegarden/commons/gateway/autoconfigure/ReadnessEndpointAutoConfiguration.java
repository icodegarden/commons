package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.gateway.endpoint.ReadnessEndPoint;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnProperty(value = "commons.gateway.endpoint.readness.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class ReadnessEndpointAutoConfiguration {

	@ConditionalOnMissingBean
	@Bean
	public ReadnessEndPoint readnessEndPoint() {
		return new ReadnessEndPoint();
	}

}
