package io.github.icodegarden.commons.springboot.configuration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import feign.Feign;
import feign.RequestInterceptor;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.filter.GatewayPreAuthenticatedAuthenticationFilter;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@ConditionalOnClass(Feign.class)
@ConditionalOnProperty(value = "commons.feign.requestInterceptor.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
public class CommonsFeignAutoConfiguration implements RequestInterceptor {
	public static final String ACCEPT_LANGUAGE = "Accept-Language";

	@Override
	public void apply(feign.RequestTemplate template) {
		template.header(WebUtils.HTTPHEADER_INTERNAL_RPC, "true");

		HttpServletRequest request = WebUtils.getRequest();
		if (request != null) {
			String language = request.getHeader(ACCEPT_LANGUAGE);
			template.header(ACCEPT_LANGUAGE, language);
		}

		String userId = SecurityUtils.getUserId();
		userId = userId != null ? userId : "sys";
		String username = SecurityUtils.getUsername();
		username = username != null ? username : "sys";

		template.header(GatewayPreAuthenticatedAuthenticationFilter.HEADER_USERID, userId);
		template.header(GatewayPreAuthenticatedAuthenticationFilter.HEADER_USERNAME, username);
	}
}