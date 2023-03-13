package io.github.icodegarden.commons.gateway.core.security.signature;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.github.icodegarden.commons.gateway.properties.CommonsGatewaySecurityProperties;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Component
public class AuthMatcher {

	private final ServerWebExchangeMatcher authMatcher;

	public AuthMatcher(CommonsGatewaySecurityProperties securityProperties) {
		CommonsGatewaySecurityProperties.Signature signature = securityProperties.getSignature();
		if (signature != null) {
			List<ServerWebExchangeMatcher> matchers = signature.getAuthPathPatterns().stream().map(path -> {
				return new PathPatternParserServerWebExchangeMatcher(path);
			}).collect(Collectors.toList());
			authMatcher = new OrServerWebExchangeMatcher(matchers);
		} else {
			authMatcher = null;
		}
	}

	/**
	 * 该认证只针对符合规范的openapi，其他的一律不做处理交给spring security识别是否需要认证<br>
	 * 因为历史开放接口可能不是按规范来的，则交给下游服务自行处理
	 */
	public boolean isAuthPath(ServerWebExchange exchange) {
		AtomicReference<Boolean> match = new AtomicReference<Boolean>();
		Mono<MatchResult> mono = authMatcher.matches(exchange);
		mono.subscribeOn(Schedulers.immediate());
		mono.subscribe(next -> {
			match.set(next.isMatch());
		});
		return match.get();
	}
}
