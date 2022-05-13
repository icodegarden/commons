package io.github.icodegarden.commons.springboot.security;

/**
 * 
 * @author Fangfang.Xu
 */
public class SimpleAuthenticationContainer implements AuthenticationContainer {

	private static final ThreadLocal<Authentication> AUTHENTICATION_HOLDER = new ThreadLocal<Authentication>();

	/**
	 * 
	 * @return Nullable
	 */
	@Override
	public Authentication getAuthentication() {
		return AUTHENTICATION_HOLDER.get();
	}

	@Override
	public void setAuthentication(Authentication authentication) {
		AUTHENTICATION_HOLDER.set(authentication);
	}

}
