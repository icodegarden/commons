package io.github.icodegarden.commons.gateway.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.icodegarden.commons.gateway.predicate.BodyMethodRoutePredicateFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class RoutePredicateFactoryAutoConfiguration {

	@Bean
	public BodyMethodRoutePredicateFactory bodyMethodRoutePredicateFactory() {
		return new BodyMethodRoutePredicateFactory();
	}

}
