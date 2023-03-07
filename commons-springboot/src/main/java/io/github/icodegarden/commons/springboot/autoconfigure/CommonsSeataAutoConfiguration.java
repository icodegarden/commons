package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import feign.RequestInterceptor;
import io.github.icodegarden.commons.springboot.seata.SeataHandlerInterceptor;
import io.github.icodegarden.commons.springboot.seata.SeataRestTemplateInterceptor;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnProperty(value = "commons.seata.springcloud.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass({ GlobalTransactional.class })
/*
 * 有这个说明引了spring-cloud-alibaba-seata，则由spring-cloud-alibaba-seata起作用
 */
@ConditionalOnMissingClass("com.alibaba.cloud.seata.web.SeataHandlerInterceptor")
@Configuration
@Slf4j
public class CommonsSeataAutoConfiguration {

	{
		log.info("commons init bean of CommonsSeataAutoConfiguration");
	}

	@ConditionalOnClass({ RequestInterceptor.class })
	@Bean
	public RequestInterceptor seataRequestInterceptor() {
		return new SeataRequestInterceptor();
	}

	@ConditionalOnWebApplication
	@Bean
	public SeataHandlerInterceptorConfigBean seataHandlerInterceptorConfigBean() {
		return new SeataHandlerInterceptorConfigBean();
	}

	@ConditionalOnClass({ RestTemplate.class })
	@Bean
	public SeataRestTemplateConfigBean seataRestTemplateConfigBean() {
		return new SeataRestTemplateConfigBean();
	}

	private class SeataRequestInterceptor implements RequestInterceptor {
		@Override
		public void apply(feign.RequestTemplate template) {
			String xid = RootContext.getXID();
			if (!StringUtils.isEmpty(xid)) {
				template.header(RootContext.KEY_XID, xid);
			}
		}
	}

	private class SeataHandlerInterceptorConfigBean implements WebMvcConfigurer {

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(new SeataHandlerInterceptor()).addPathPatterns("/**");
		}
	}

	private class SeataRestTemplateConfigBean {

		@Autowired(required = false)
		private Collection<RestTemplate> restTemplates;

		@PostConstruct
		public void init() {
			if (this.restTemplates != null) {
				SeataRestTemplateInterceptor seataRestTemplateInterceptor = new SeataRestTemplateInterceptor();
				
				for (RestTemplate restTemplate : restTemplates) {
					List<ClientHttpRequestInterceptor> interceptors = new ArrayList<ClientHttpRequestInterceptor>(
							restTemplate.getInterceptors());
					interceptors.add(seataRestTemplateInterceptor);
					restTemplate.setInterceptors(interceptors);
				}
			}
		}

	}

}