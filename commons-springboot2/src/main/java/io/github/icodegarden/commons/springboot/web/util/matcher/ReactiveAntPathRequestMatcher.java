package io.github.icodegarden.commons.springboot.web.util.matcher;

import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public final class ReactiveAntPathRequestMatcher implements ReactiveRequestMatcher {

	private static final String MATCH_ALL = "/**";

	private final AntPathMatcher matcher;

	private final String pattern;

	private final HttpMethod httpMethod;

	public ReactiveAntPathRequestMatcher(String pattern) {
		this(pattern, null);
	}

	public ReactiveAntPathRequestMatcher(String pattern, String httpMethod) {
		this(pattern, httpMethod, true);
	}

	public ReactiveAntPathRequestMatcher(String pattern, String httpMethod, boolean caseSensitive) {
		Assert.hasText(pattern, "Pattern cannot be null or empty");
		if (pattern.equals(MATCH_ALL) || pattern.equals("**")) {
			pattern = MATCH_ALL;
			this.matcher = null;
		} else {
			this.matcher = new AntPathMatcher();
			this.matcher.setTrimTokens(false);
			this.matcher.setCaseSensitive(caseSensitive);
		}

		this.pattern = pattern;
		this.httpMethod = StringUtils.hasText(httpMethod) ? HttpMethod.valueOf(httpMethod) : null;
	}

	@Override
	public boolean matches(ServerWebExchange exchange) {
		if (this.httpMethod != null && exchange.getRequest().getMethod() != null
				&& this.httpMethod != exchange.getRequest().getMethod()) {
			return false;
		}
		if (this.pattern.equals(MATCH_ALL)) {
			return true;
		}
		String url = exchange.getRequest().getPath().value();
		return this.matcher.match(pattern, url);
	}

	public String getPattern() {
		return this.pattern;
	}

}
