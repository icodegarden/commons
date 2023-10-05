package io.github.icodegarden.commons.gateway.core.security.signature;

import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;

import io.github.icodegarden.commons.gateway.spi.AppProvider;
import io.github.icodegarden.commons.gateway.spi.OpenApiRequestValidator;
import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class SignatureAuthenticationConfig {
	private ServerCodecConfigurer codecConfigurer;
//		private Set<String> acceptPathPatterns;
	private AppProvider appProvider;
	private OpenApiRequestValidator openApiRequestValidator;
	private ReactiveAuthenticationManager authenticationManager;
	private ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler;
	private ServerAuthenticationFailureHandler serverAuthenticationFailureHandler;

	public SignatureAuthenticationConfig(ServerCodecConfigurer codecConfigurer, AppProvider appProvider,
			OpenApiRequestValidator openApiRequestValidator, ReactiveAuthenticationManager authenticationManager,
			ServerAuthenticationSuccessHandler serverAuthenticationSuccessHandler,
			ServerAuthenticationFailureHandler serverAuthenticationFailureHandler) {
		this.codecConfigurer = codecConfigurer;
		this.appProvider = appProvider;
		this.openApiRequestValidator = openApiRequestValidator;
		this.authenticationManager = authenticationManager;
		this.serverAuthenticationSuccessHandler = serverAuthenticationSuccessHandler;
		this.serverAuthenticationFailureHandler = serverAuthenticationFailureHandler;
	}
}