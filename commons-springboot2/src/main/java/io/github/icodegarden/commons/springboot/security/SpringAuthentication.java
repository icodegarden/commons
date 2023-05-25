package io.github.icodegarden.commons.springboot.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SpringAuthentication implements Authentication, org.springframework.security.core.Authentication {
	private static final long serialVersionUID = 1L;

	private final org.springframework.security.core.Authentication delegator;

	public SpringAuthentication(org.springframework.security.core.Authentication authentication) {
		this.delegator = authentication;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return delegator.getAuthorities();
	}

	@Override
	public Object getPrincipal() {
		return delegator.getPrincipal();
	}

	@Override
	public String getName() {
		return delegator.getName();
	}

	@Override
	public Object getCredentials() {
		return delegator.getCredentials();
	}

	@Override
	public Object getDetails() {
		return delegator.getDetails();
	}

	@Override
	public boolean isAuthenticated() {
		return delegator.isAuthenticated();
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		delegator.setAuthenticated(isAuthenticated);
	}
}
