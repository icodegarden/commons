package io.github.icodegarden.commons.springboot.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.springboot.GracefullyShutdownLifecycle;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class CommonsBeanAutoConfiguration {

	@Bean
	public SpringContext springContext() {
		return new SpringContext();
	}

	@ConditionalOnProperty(value = "commons.bean.lifecycle.gracefullyShutdown.enabled", havingValue = "true", matchIfMissing = true)
	@Bean
	public SmartLifecycle gracefullyShutdownLifecycle() {
		return new GracefullyShutdownLifecycle();
	}

}
