package io.github.icodegarden.commons.springboot.web.util.matcher;

import java.util.Arrays;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public final class ReactiveOrRequestMatcher implements ReactiveRequestMatcher {

	private final List<ReactiveRequestMatcher> requestMatchers;

	public ReactiveOrRequestMatcher(List<ReactiveRequestMatcher> requestMatchers) {
		Assert.notEmpty(requestMatchers, "requestMatchers must contain a value");
		Assert.isTrue(!requestMatchers.contains(null), "requestMatchers cannot contain null values");
		this.requestMatchers = requestMatchers;
	}

	public ReactiveOrRequestMatcher(ReactiveRequestMatcher... requestMatchers) {
		this(Arrays.asList(requestMatchers));
	}

	@Override
	public boolean matches(ServerWebExchange exchange) {
		for (ReactiveRequestMatcher matcher : this.requestMatchers) {
			if (matcher.matches(exchange)) {
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