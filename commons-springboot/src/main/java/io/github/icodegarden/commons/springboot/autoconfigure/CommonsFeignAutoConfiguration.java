package io.github.icodegarden.commons.springboot.autoconfigure;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.RequestInterceptor;
import io.github.icodegarden.commons.springboot.properties.CommonsFeignProperties;
import io.github.icodegarden.commons.springboot.properties.CommonsFeignProperties.Header;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
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
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	{
		log.info("commons init bean of CommonsFeignAutoConfiguration");
	}
	
	@Autowired
	private CommonsFeignProperties commonsFeignProperties;

	@Override
	public void apply(feign.RequestTemplate template) {
		Header header = commonsFeignProperties.getHeader();

		HttpServletRequest request = WebUtils.getRequest();

		if (request != null) {
			if (header.isTransferAll()) {
				Enumeration<String> headerNames = request.getHeaderNames();
				while (headerNames.hasMoreElements()) {
					String headerName = headerNames.nextElement();
					String headerValue = request.getHeader(headerName);
					template.header(headerName, headerValue);// 只需单值
				}
			}

			String language = request.getHeader(ACCEPT_LANGUAGE);
			template.header(ACCEPT_LANGUAGE, language);
		}

		template.header(WebUtils.HEADER_INTERNAL_RPC, "true");

		String userId = SecurityUtils.getUserId();
		userId = userId != null ? userId : header.getUserIdIfNotPresent();
		String username = SecurityUtils.getUsername();
		username = username != null ? username : header.getUsernameIfNotPresent();

		template.header(WebUtils.HEADER_USERID, userId);
		template.header(WebUtils.HEADER_USERNAME, username);
	}
}