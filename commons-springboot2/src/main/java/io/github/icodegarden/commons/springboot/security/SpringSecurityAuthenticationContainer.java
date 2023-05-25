package io.github.icodegarden.commons.springboot.security;

import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 
 * @author Fangfang.Xu
 */
public class SpringSecurityAuthenticationContainer implements AuthenticationContainer {

	/**
	 * 
	 * @return Nullable
	 */
	@Override
	public Authentication getAuthentication() {
		return (Authentication) SecurityContextHolder.getContext().getAuthentication();
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		SecurityContextHolder.getContext().setAuthentication((SpringAuthentication) authentication);
	}

}
