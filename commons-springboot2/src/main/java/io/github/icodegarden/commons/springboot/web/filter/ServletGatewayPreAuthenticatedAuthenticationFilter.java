package io.github.icodegarden.commons.springboot.web.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.util.UrlPathHelper;

import io.github.icodegarden.commons.springboot.security.Authentication;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.security.SimpleAuthentication;
import io.github.icodegarden.commons.springboot.security.SimpleUser;
import io.github.icodegarden.commons.springboot.web.util.ServletWebUtils;
import io.github.icodegarden.commons.springboot.web.util.WebUtils;

/**
 * 适用于spring-web<br>
 * 
 * 网关预认证的身份filter<br>
 * 
 * @author Fangfang.Xu
 */
public class ServletGatewayPreAuthenticatedAuthenticationFilter extends GenericFilterBean {

//	private ResponseType responseType = ResponseType.ApiResponse;

	private OrRequestMatcher shouldAuthOpenapiMatcher;
	private OrRequestMatcher shouldAuthInternalApiMatcher;

	/**
	 * 默认认为POST /openapi/**、/api/** 应该已经pre认证了
	 */
	public ServletGatewayPreAuthenticatedAuthenticationFilter() {
		setShouldAuthOpenapi(Arrays.asList(new AntPath("/openapi/**", "POST")));
		setShouldAuthInternalApi(Arrays.asList(new AntPath("/api/**", null), new AntPath("/internalapi/**", null),
				new AntPath("/innerapi/**", null)));
	}

//	public void setResponseType(ResponseType responseType) {
//		this.responseType = responseType;
//	}

	/**
	 * path以 / 开头,method可以null表示无感<br>
	 * path例如/**、/xxxx/**、/file/api/v1/softwareParts/{id}/files/{filename}/upload
	 * 
	 * @param antPaths
	 */
	public void setShouldAuthOpenapi(Collection<AntPath> antPaths) {
		List<AntPathRequestMatcher> ants = antPaths.stream()
				.map(antPath -> new AntPathRequestMatcher(antPath.getPattern(), antPath.getHttpMethod()))
				.collect(Collectors.toList());
		shouldAuthOpenapiMatcher = new OrRequestMatcher(ants);
	}

	/**
	 * path以 / 开头,method可以null表示无感<br>
	 * path例如/**、/xxxx/**、/file/api/v1/softwareParts/{id}/files/{filename}/upload
	 * 
	 * @param antPaths
	 */
	public void setShouldAuthInternalApi(Collection<AntPath> antPaths) {
		List<AntPathRequestMatcher> ants = antPaths.stream()
				.map(antPath -> new AntPathRequestMatcher(antPath.getPattern(), antPath.getHttpMethod()))
				.collect(Collectors.toList());
		shouldAuthInternalApiMatcher = new OrRequestMatcher(ants);
	}

	private boolean shouldAuthOpenapi(HttpServletRequest request) {
		return shouldAuthOpenapiMatcher.matches(request);
	}

	private boolean shouldAuthInternalApi(HttpServletRequest request) {
		return shouldAuthInternalApiMatcher.matches(request);
	}

//	public static enum ResponseType {
//		ApiResponse, NativeRestApi
//	}

	public static class AntPath {
		private final String pattern;
		private final String httpMethod;

		public AntPath(String pattern, @Nullable String httpMethod) {
			Assert.hasText(pattern, "pattern must not empty");
			this.pattern = pattern;
			this.httpMethod = httpMethod != null ? httpMethod.toUpperCase() : null;
		}

		public String getPattern() {
			return pattern;
		}

		public String getHttpMethod() {
			return httpMethod;
		}

		@Override
		public String toString() {
			return "AntPath [pattern=" + pattern + ", httpMethod=" + httpMethod + "]";
		}
	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;

		String appId = request.getHeader(WebUtils.HEADER_APPID);
		if (appId == null && shouldAuthOpenapi(request)) {
			/**
			 * 不需要以ApiResponse返回，因为这是gateway犯的错
			 */
			ServletWebUtils.responseWrite(401, "Access Denied, Unauthorized, App No Principal", response);
			return;
		}

		String userId = null;
		if (appId == null) {
			userId = request.getHeader(WebUtils.HEADER_USERID);
			if (userId == null && shouldAuthInternalApi(request)) {
				/**
				 * 不需要以ApiResponse返回，因为这是gateway犯的错
				 */
				ServletWebUtils.responseWrite(401, "Access Denied, Unauthorized, User No Principal", response);
				return;
			}
		}

		try {
			Authentication authentication = null;
			if (appId != null) {
				String appName = request.getHeader(WebUtils.HEADER_APPNAME);

				SimpleUser user = new SimpleUser(appId, appName, "", Collections.emptyList());
				authentication = new SimpleAuthentication(user, Collections.emptyList());
			} else if (userId != null) {
				String userName = request.getHeader(WebUtils.HEADER_USERNAME);

				SimpleUser user = new SimpleUser(userId, userName, "", Collections.emptyList());
				authentication = new SimpleAuthentication(user, Collections.emptyList());
			}
			if(authentication != null) {
				SecurityUtils.setAuthentication(authentication);
			}

			filterChain.doFilter(request, response);
		} finally {
			SecurityUtils.setAuthentication(null);
		}
	}

//	private void responseUnauthorized(String identityType,HttpServletResponse response) throws IOException {
//		if(responseType == ResponseType.ApiResponse) {
//		}else if(responseType == ResponseType.NativeRestApi) {
//			WebUtils.responseWrite(401, String.format("Unauthorized, %s No Principal",identityType), response);
//		}else {
//			throw new IllegalStateException(String.format("ResponseType %s Not Support",responseType));
//		}
//	}

	// 为了适应不依赖spring-security的项目，以下代码copy spring-security
	// --------------------------------------------
	/**
	 * {@link RequestMatcher} that will return true if any of the passed in
	 * {@link RequestMatcher} instances match.
	 *
	 * @author Rob Winch
	 * @since 3.2
	 */
	private static class OrRequestMatcher {

		private final List<AntPathRequestMatcher> requestMatchers;

		/**
		 * Creates a new instance
		 * 
		 * @param requestMatchers the {@link RequestMatcher} instances to try
		 */
		public OrRequestMatcher(List<AntPathRequestMatcher> requestMatchers) {
			Assert.notEmpty(requestMatchers, "requestMatchers must contain a value");
			Assert.isTrue(!requestMatchers.contains(null), "requestMatchers cannot contain null values");
			this.requestMatchers = requestMatchers;
		}

		/**
		 * Creates a new instance
		 * 
		 * @param requestMatchers the {@link RequestMatcher} instances to try
		 */
		public OrRequestMatcher(AntPathRequestMatcher... requestMatchers) {
			this(Arrays.asList(requestMatchers));
		}

		public boolean matches(HttpServletRequest request) {
			for (AntPathRequestMatcher matcher : this.requestMatchers) {
				if (matcher.matches(request)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return "Or " + this.requestMatchers;
		}

	}

	/**
	 * Matcher which compares a pre-defined ant-style pattern against the URL (
	 * {@code servletPath + pathInfo}) of an {@code HttpServletRequest}. The query
	 * string of the URL is ignored and matching is case-insensitive or
	 * case-sensitive depending on the arguments passed into the constructor.
	 * <p>
	 * Using a pattern value of {@code /**} or {@code **} is treated as a universal
	 * match, which will match any request. Patterns which end with {@code /**} (and
	 * have no other wildcards) are optimized by using a substring match &mdash; a
	 * pattern of {@code /aaa/**} will match {@code /aaa}, {@code /aaa/} and any
	 * sub-directories, such as {@code /aaa/bbb/ccc}.
	 * </p>
	 * <p>
	 * For all other cases, Spring's {@link AntPathMatcher} is used to perform the
	 * match. See the Spring documentation for this class for comprehensive
	 * information on the syntax used.
	 * </p>
	 *
	 * @author Luke Taylor
	 * @author Rob Winch
	 * @author Eddú Meléndez
	 * @author Evgeniy Cheban
	 * @since 3.1
	 * @see org.springframework.util.AntPathMatcher
	 */
	private static class AntPathRequestMatcher {

		private static final String MATCH_ALL = "/**";

		private final Matcher matcher;

		private final String pattern;

		private final HttpMethod httpMethod;

		private final boolean caseSensitive;

		private final UrlPathHelper urlPathHelper;

		/**
		 * Creates a matcher with the specific pattern which will match all HTTP methods
		 * in a case sensitive manner.
		 * 
		 * @param pattern the ant pattern to use for matching
		 */
		public AntPathRequestMatcher(String pattern) {
			this(pattern, null);
		}

		/**
		 * Creates a matcher with the supplied pattern and HTTP method in a case
		 * sensitive manner.
		 * 
		 * @param pattern    the ant pattern to use for matching
		 * @param httpMethod the HTTP method. The {@code matches} method will return
		 *                   false if the incoming request doesn't have the same method.
		 */
		public AntPathRequestMatcher(String pattern, String httpMethod) {
			this(pattern, httpMethod, true);
		}

		/**
		 * Creates a matcher with the supplied pattern which will match the specified
		 * Http method
		 * 
		 * @param pattern       the ant pattern to use for matching
		 * @param httpMethod    the HTTP method. The {@code matches} method will return
		 *                      false if the incoming request doesn't doesn't have the
		 *                      same method.
		 * @param caseSensitive true if the matcher should consider case, else false
		 */
		public AntPathRequestMatcher(String pattern, String httpMethod, boolean caseSensitive) {
			this(pattern, httpMethod, caseSensitive, null);
		}

		/**
		 * Creates a matcher with the supplied pattern which will match the specified
		 * Http method
		 * 
		 * @param pattern       the ant pattern to use for matching
		 * @param httpMethod    the HTTP method. The {@code matches} method will return
		 *                      false if the incoming request doesn't have the same
		 *                      method.
		 * @param caseSensitive true if the matcher should consider case, else false
		 * @param urlPathHelper if non-null, will be used for extracting the path from
		 *                      the HttpServletRequest
		 */
		public AntPathRequestMatcher(String pattern, String httpMethod, boolean caseSensitive,
				UrlPathHelper urlPathHelper) {
			Assert.hasText(pattern, "Pattern cannot be null or empty");
			this.caseSensitive = caseSensitive;
			if (pattern.equals(MATCH_ALL) || pattern.equals("**")) {
				pattern = MATCH_ALL;
				this.matcher = null;
			} else {
				// If the pattern ends with {@code /**} and has no other wildcards or path
				// variables, then optimize to a sub-path match
				if (pattern.endsWith(MATCH_ALL)
						&& (pattern.indexOf('?') == -1 && pattern.indexOf('{') == -1 && pattern.indexOf('}') == -1)
						&& pattern.indexOf("*") == pattern.length() - 2) {
					this.matcher = new SubpathMatcher(pattern.substring(0, pattern.length() - 3), caseSensitive);
				} else {
					this.matcher = new SpringAntMatcher(pattern, caseSensitive);
				}
			}
			this.pattern = pattern;
			this.httpMethod = StringUtils.hasText(httpMethod) ? HttpMethod.valueOf(httpMethod) : null;
			this.urlPathHelper = urlPathHelper;
		}

		/**
		 * Returns true if the configured pattern (and HTTP-Method) match those of the
		 * supplied request.
		 * 
		 * @param request the request to match against. The ant pattern will be matched
		 *                against the {@code servletPath} + {@code pathInfo} of the
		 *                request.
		 */
		public boolean matches(HttpServletRequest request) {
			if (this.httpMethod != null && StringUtils.hasText(request.getMethod())
					&& this.httpMethod != HttpMethod.resolve(request.getMethod())) {
				return false;
			}
			if (this.pattern.equals(MATCH_ALL)) {
				return true;
			}
			String url = getRequestPath(request);
			return this.matcher.matches(url);
		}

		@Deprecated
		public Map<String, String> extractUriTemplateVariables(HttpServletRequest request) {
			return matcher(request).getVariables();
		}

		public MatchResult matcher(HttpServletRequest request) {
			if (!matches(request)) {
				return MatchResult.notMatch();
			}
			if (this.matcher == null) {
				return MatchResult.match();
			}
			String url = getRequestPath(request);
			return MatchResult.match(this.matcher.extractUriTemplateVariables(url));
		}

		private String getRequestPath(HttpServletRequest request) {
			if (this.urlPathHelper != null) {
				return this.urlPathHelper.getPathWithinApplication(request);
			}
			String url = request.getServletPath();
			String pathInfo = request.getPathInfo();
			if (pathInfo != null) {
				url = StringUtils.hasLength(url) ? url + pathInfo : pathInfo;
			}
			return url;
		}

		public String getPattern() {
			return this.pattern;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AntPathRequestMatcher)) {
				return false;
			}
			AntPathRequestMatcher other = (AntPathRequestMatcher) obj;
			return this.pattern.equals(other.pattern) && this.httpMethod == other.httpMethod
					&& this.caseSensitive == other.caseSensitive;
		}

		@Override
		public int hashCode() {
			int result = (this.pattern != null) ? this.pattern.hashCode() : 0;
			result = 31 * result + ((this.httpMethod != null) ? this.httpMethod.hashCode() : 0);
			result = 31 * result + (this.caseSensitive ? 1231 : 1237);
			return result;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Ant [pattern='").append(this.pattern).append("'");
			if (this.httpMethod != null) {
				sb.append(", ").append(this.httpMethod);
			}
			sb.append("]");
			return sb.toString();
		}

		private interface Matcher {

			boolean matches(String path);

			Map<String, String> extractUriTemplateVariables(String path);

		}

		private final class SpringAntMatcher implements Matcher {

			private final AntPathMatcher antMatcher;

			private final String pattern;

			private SpringAntMatcher(String pattern, boolean caseSensitive) {
				this.pattern = pattern;
				this.antMatcher = createMatcher(caseSensitive);
			}

			@Override
			public boolean matches(String path) {
				return this.antMatcher.match(this.pattern, path);
			}

			@Override
			public Map<String, String> extractUriTemplateVariables(String path) {
				return this.antMatcher.extractUriTemplateVariables(this.pattern, path);
			}

			private AntPathMatcher createMatcher(boolean caseSensitive) {
				AntPathMatcher matcher = new AntPathMatcher();
				matcher.setTrimTokens(false);
				matcher.setCaseSensitive(caseSensitive);
				return matcher;
			}

		}

		/**
		 * Optimized matcher for trailing wildcards
		 */
		private final class SubpathMatcher implements Matcher {

			private final String subpath;

			private final int length;

			private final boolean caseSensitive;

			private SubpathMatcher(String subpath, boolean caseSensitive) {
				Assert.isTrue(!subpath.contains("*"), "subpath cannot contain \"*\"");
				this.subpath = caseSensitive ? subpath : subpath.toLowerCase();
				this.length = subpath.length();
				this.caseSensitive = caseSensitive;
			}

			@Override
			public boolean matches(String path) {
				if (!this.caseSensitive) {
					path = path.toLowerCase();
				}
				return path.startsWith(this.subpath)
						&& (path.length() == this.length || path.charAt(this.length) == '/');
			}

			@Override
			public Map<String, String> extractUriTemplateVariables(String path) {
				return Collections.emptyMap();
			}

		}

	}

	/**
	 * The result of matching against an HttpServletRequest Contains the status,
	 * true or false, of the match and if present, any variables extracted from the
	 * match
	 *
	 * @since 5.2
	 */
	private static class MatchResult {

		private final boolean match;

		private final Map<String, String> variables;

		MatchResult(boolean match, Map<String, String> variables) {
			this.match = match;
			this.variables = variables;
		}

		/**
		 * @return true if the comparison against the HttpServletRequest produced a
		 *         successful match
		 */
		public boolean isMatch() {
			return this.match;
		}

		/**
		 * Returns the extracted variable values where the key is the variable name and
		 * the value is the variable value
		 * 
		 * @return a map containing key-value pairs representing extracted variable
		 *         names and variable values
		 */
		public Map<String, String> getVariables() {
			return this.variables;
		}

		/**
		 * Creates an instance of {@link MatchResult} that is a match with no variables
		 * 
		 * @return
		 */
		public static MatchResult match() {
			return new MatchResult(true, Collections.emptyMap());
		}

		/**
		 * Creates an instance of {@link MatchResult} that is a match with the specified
		 * variables
		 * 
		 * @param variables
		 * @return
		 */
		public static MatchResult match(Map<String, String> variables) {
			return new MatchResult(true, variables);
		}

		/**
		 * Creates an instance of {@link MatchResult} that is not a match.
		 * 
		 * @return
		 */
		public static MatchResult notMatch() {
			return new MatchResult(false, Collections.emptyMap());
		}

	}
}
