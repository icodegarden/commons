package io.github.icodegarden.commons.springboot.security;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * @author Fangfang.Xu
 */
public class Http403AccessDeniedHandler implements AccessDeniedHandler {

//	private static final Logger log = LoggerFactory.getLogger(AccessDeniedHandler.class);

	private String errorPage;

	/**
	 * 当授权不通过时
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException, ServletException {
		if (!response.isCommitted()) {
			if (errorPage != null) {
				request.setAttribute(WebAttributes.ACCESS_DENIED_403, accessDeniedException);

				response.setStatus(HttpServletResponse.SC_FORBIDDEN);

				RequestDispatcher dispatcher = request.getRequestDispatcher(errorPage);
				dispatcher.forward(request, response);
			} else {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				response.setContentType("application/json;charset=utf-8");
				response.getWriter().println("Access Denied, Forbidden Authority");
			}
		}
	}

	public void setErrorPage(String errorPage) {
		if ((errorPage != null) && !errorPage.startsWith("/")) {
			throw new IllegalArgumentException("errorPage must begin with '/'");
		}
		this.errorPage = errorPage;
	}
}
