package io.github.icodegarden.commons.springboot.security;

import java.util.Collection;

import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SpringUser extends org.springframework.security.core.userdetails.User implements User {
	private static final long serialVersionUID = 1L;

	private String userId;

	public SpringUser(@Nullable String userId, String username, String password,
			Collection<? extends GrantedAuthority> authorities) {
		this(userId, username, password, true, true, true, true, authorities);
	}

	public SpringUser(@Nullable String userId, String username, String password, boolean enabled,
			boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.userId = userId;
	}

	@Override
	public String getUserId() {
		return userId;
	}
}
