package io.github.icodegarden.beecomb.test.web.controller;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
public class Configuration4Tests {

	@Bean
	public FilterRegistrationBean<Filter> gatewayPreAuthenticatedAuthenticationFilter() {
		GatewayPreAuthenticatedAuthenticationFilter filter = new GatewayPreAuthenticatedAuthenticationFilter();
//		filter.setShouldAuthOpenapi(Arrays.asList(new AntPath("/op2enapi/**", "POST")));
//		filter.setShouldAuthInternalApi(Arrays.asList(new AntPath("/a2pi/**", null)));

		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<Filter>();
		bean.setFilter(filter);
		bean.setName("auth");
		bean.addUrlPatterns("/*");
		bean.setOrder(Ordered.HIGHEST_PRECEDENCE);

		return bean;
	}
}
