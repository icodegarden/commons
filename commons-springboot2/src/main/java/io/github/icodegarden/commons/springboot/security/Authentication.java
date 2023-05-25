package io.github.icodegarden.commons.springboot.security;

import java.util.Collection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Authentication {

	String getName();

	Collection<? extends Object> getAuthorities();

	Object getCredentials();

	Object getDetails();

	Object getPrincipal();

	boolean isAuthenticated();

	void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException;
}
