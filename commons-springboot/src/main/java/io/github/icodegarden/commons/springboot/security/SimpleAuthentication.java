package io.github.icodegarden.commons.springboot.security;

import java.util.Collection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SimpleAuthentication implements Authentication {

	private Collection<String> authorities;
	private SimpleUser user;

	public SimpleAuthentication(SimpleUser user, Collection<String> authorities) {
		this.user = user;
		this.authorities = authorities;
	}

	public Collection<? extends Object> getAuthorities() {
		return authorities;
	}

	public SimpleUser getPrincipal() {
		return user;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Object getCredentials() {
		return null;
	}

	@Override
	public Object getDetails() {
		return null;
	}

	@Override
	public boolean isAuthenticated() {
		return true;
	}

	@Override
	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
	}
}
