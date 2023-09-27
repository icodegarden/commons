package io.github.icodegarden.commons.springboot.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 适用于spring-web<br>
 * 
 * 可以统计处理中的请求数，可以优雅停机<br>
 * 这个类的shutdownOrder数值应该配置的比 服务注销 的GracefullyShutdown数值大
 * 
 * @author Fangfang.Xu
 *
 */
public class ServletProcessingRequestCountFilter extends AbstractProcessingRequestCount implements Filter {

	public ServletProcessingRequestCountFilter(int gracefullyShutdownOrder, long instanceRefreshIntervalMs) {
		super(gracefullyShutdownOrder, instanceRefreshIntervalMs);
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (closed) {
			/**
			 * 此时client收到的将是500
			 * 
			 * body：<br>
			 * <html><body>
			 * <h1>Whitelabel Error Page</h1>
			 * <p>
			 * This application has no explicit mapping for /error, so you are seeing this
			 * as a fallback.
			 * </p>
			 * <div id='created'>Fri Feb 11 17:26:57 CST 2022</div><div>There was an
			 * unexpected error (type=Internal Server Error,
			 * status=500).</div></body></html>
			 */
			throw new ServletException(shutdownName() + " was closed");
		}
		try {
			count.incrementAndGet();

			chain.doFilter(request, response);
		} finally {
			count.decrementAndGet();

//			long c = count.decrementAndGet();
//			if (c <= 0) {
//				synchronized (this) {
//					this.notify();
//				}
//			}
		}
	}

	@Override
	public String shutdownName() {
		return "Processing-Request-Count-Filter";
	}
}