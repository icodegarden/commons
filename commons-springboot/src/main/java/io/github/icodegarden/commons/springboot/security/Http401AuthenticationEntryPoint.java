package io.github.icodegarden.commons.springboot.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Http401AuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(Http401AuthenticationEntryPoint.class);

	/**
	 * 认证失败时
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		if (!response.isCommitted()) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json;charset=utf-8");
//			response.getWriter().write("Access Denied, Unauthorized");

			String message = e.getMessage() != null ? e.getMessage() : "Not Authenticated.";

			if (log.isInfoEnabled()) {
				log.info("request {}", message);
			}
			response.getWriter().write(e.getMessage() != null ? e.getMessage() : "Not Authenticated.");
		}
	}
}
