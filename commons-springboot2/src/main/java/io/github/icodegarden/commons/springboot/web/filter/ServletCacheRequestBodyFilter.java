package io.github.icodegarden.commons.springboot.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

/**
 * ContentCachingRequestWrapper 功能
 * 
 * @author Fangfang.Xu
 *
 */
public class ServletCacheRequestBodyFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ContentCachingRequestWrapper wrapper = null;
		if (request instanceof ContentCachingRequestWrapper) {
			wrapper = (ContentCachingRequestWrapper) request;
		} else {
			wrapper = new ContentCachingRequestWrapper(request);
		}
		filterChain.doFilter(wrapper, response);
	}
}
