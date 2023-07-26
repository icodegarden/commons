package io.github.icodegarden.commons.springboot.web.util.matcher;

import org.springframework.web.server.ServerWebExchange;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ReactiveRequestMatcher {

	boolean matches(ServerWebExchange exchange);
}
