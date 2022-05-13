package io.github.icodegarden.commons.springboot.security;

import org.springframework.util.ClassUtils;

/**
 * 
 * @author Fangfang.Xu
 */
public abstract class SecurityUtils {

	private static AuthenticationContainer authenticationContainer;

	static {
		boolean present = ClassUtils.isPresent("org.springframework.security.core.Authentication",
				ClassUtils.getDefaultClassLoader());
		if (present) {
			authenticationContainer = new SpringSecurityAuthenticationContainer();
		} else {
			authenticationContainer = new SimpleAuthenticationContainer();
		}
	}

	public static void configAuthenticationContainer(AuthenticationContainer authenticationContainer) {
		SecurityUtils.authenticationContainer = authenticationContainer;
	}

	/**
	 * 
	 * @return Nullable
	 */
	public static Authentication getAuthentication() {
		return authenticationContainer.getAuthentication();
	}

	/**
	 * 
	 * @return Nullable
	 */
	public static User getAuthenticatedUser() {
		return authenticationContainer.getAuthenticatedUser();
	}

	/**
	 * 
	 * @return Nullable
	 */
	public static String getUserId() {
		User user = getAuthenticatedUser();
		if (user != null) {
			return user.getUserId();
		}
		return null;
	}

	/**
	 * 
	 * @return Nullable
	 */
	public static String getUsername() {
		User user = getAuthenticatedUser();
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	public static void setAuthentication(Authentication authentication) {
		authenticationContainer.setAuthentication(authentication);
	}

}
