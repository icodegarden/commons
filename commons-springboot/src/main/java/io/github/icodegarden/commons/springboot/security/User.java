package io.github.icodegarden.commons.springboot.security;

import java.util.Collection;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface User {

	String getUserId();
	
	String getUsername();
	
	String getPassword();
	
	Collection<? extends Object> getAuthorities();

	boolean isAccountNonExpired();

	boolean isAccountNonLocked();

	boolean isCredentialsNonExpired();

	boolean isEnabled();

}
