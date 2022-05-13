package io.github.icodegarden.beecomb.test.web;

import javax.servlet.Filter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootApplication
public class TestWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestWebApplication.class, args);
	}

}