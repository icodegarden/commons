package io.github.icodegarden.commons.springboot.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * use {@link SecurityUtils}
 * @author Fangfang.Xu
 */
@Deprecated
public abstract class SpringSecurityUtils {

	/**
	 * 
	 * @return Nullable
	 */
	public static Authentication getAuthentication() {
		return SecurityContextHolder.getContext().getAuthentication();
	}

	/**
	 * 
	 * @return Nullable
	 */
	public static User getAuthenticatedUser() {
		Authentication authentication = getAuthentication();
		if (authentication != null) {
			Object principal = authentication.getPrincipal();
			if (principal instanceof User) {
				return (User) principal;
			}
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
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

}
