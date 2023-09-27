package io.github.icodegarden.commons.springboot.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NativeRestApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private static final Logger log = LoggerFactory.getLogger(NativeRestApiAuthenticationEntryPoint.class);

	/**
	 * 认证失败时
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e)
			throws IOException, ServletException {
		if (!response.isCommitted()) {
			String message = e.getMessage() != null ? e.getMessage() : "Not Authenticated.";

			if (log.isInfoEnabled()) {
				log.info("request {}", message);
			}
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			response.setCharacterEncoding("utf-8");
//			response.getWriter().write("Access Denied, Unauthorized");
			response.getWriter().write(e.getMessage() != null ? e.getMessage() : "Not Authenticated.");
		}
	}
}
