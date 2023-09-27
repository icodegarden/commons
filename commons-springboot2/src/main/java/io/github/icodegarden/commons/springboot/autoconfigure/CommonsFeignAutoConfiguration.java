package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import feign.Feign;
import feign.RequestInterceptor;
import io.github.icodegarden.commons.springboot.properties.CommonsFeignProperties;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.BaseWebUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(Feign.class)
@ConditionalOnProperty(value = "commons.feign.requestInterceptor.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties({ CommonsFeignProperties.class })
@Configuration
@Slf4j
public class CommonsFeignAutoConfiguration implements RequestInterceptor {

	{
		log.info("commons init bean of CommonsFeignAutoConfiguration");
	}

	@Autowired
	private CommonsFeignProperties commonsFeignProperties;

	@Override
	public void apply(feign.RequestTemplate template) {
		CommonsFeignProperties.Header headerConfig = commonsFeignProperties.getHeader();

		if (headerConfig.isTransferAll()) {
			List<String> headerNames = WebUtils.getHeaderNames();
			if (!CollectionUtils.isEmpty(headerNames)) {
				for (String headerName : headerNames) {
					String headerValue = WebUtils.getHeader(headerName);
					template.header(headerName, headerValue);// 只需单值
				}
			}
		}

		String language = WebUtils.getHeader(BaseWebUtils.HEADER_ACCEPT_LANGUAGE);
		if (language != null) {
			template.header(BaseWebUtils.HEADER_ACCEPT_LANGUAGE, language);
		}
		String isApiRequest = WebUtils.getHeader(BaseWebUtils.HEADER_API_REQUEST);
		if (language != null) {
			template.header(BaseWebUtils.HEADER_API_REQUEST, isApiRequest);
		}
		String isOpenapiRequest = WebUtils.getHeader(BaseWebUtils.HEADER_OPENAPI_REQUEST);
		if (language != null) {
			template.header(BaseWebUtils.HEADER_OPENAPI_REQUEST, isOpenapiRequest);
		}

		template.header(WebUtils.HEADER_INTERNAL_RPC, "true");

		String userId = SecurityUtils.getUserId();
		userId = userId != null ? userId : headerConfig.getUserIdIfNotPresent();
		String username = SecurityUtils.getUsername();
		username = username != null ? username : headerConfig.getUsernameIfNotPresent();

		template.header(WebUtils.HEADER_USERID, userId);
		template.header(WebUtils.HEADER_USERNAME, username);
	}
}