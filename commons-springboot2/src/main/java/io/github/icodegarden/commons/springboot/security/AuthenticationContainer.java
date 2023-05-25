package io.github.icodegarden.commons.springboot.security;

/**
 * 
 * @author Fangfang.Xu
 */
interface AuthenticationContainer {

	/**
	 * 
	 * @return Nullable
	 */
	Authentication getAuthentication();

	/**
	 * 
	 * @return Nullable
	 */
	default User getAuthenticatedUser() {
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
	default String getUsername() {
		User user = getAuthenticatedUser();
		if (user != null) {
			return user.getUsername();
		}
		return null;
	}

	void setAuthentication(Authentication authentication);

}
