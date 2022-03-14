package io.github.icodegarden.commons.springboot.web.filter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;

/**
 * 可以统计处理中的web请求数，可以优雅停机
 * @author Fangfang.Xu
 *
 */
public class ProcessingRequestCountFilter implements Filter, GracefullyShutdown {

	private static final Logger log = LoggerFactory.getLogger(ProcessingRequestCountFilter.class);

	private AtomicLong count = new AtomicLong(0);
	private volatile boolean closed;
	private final int gracefullyShutdownOrder;
	private final long maxWaitMillisOnShutdown;

	public ProcessingRequestCountFilter(int gracefullyShutdownOrder, long maxWaitMillisOnShutdown) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
		this.maxWaitMillisOnShutdown = maxWaitMillisOnShutdown;

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
			long c = count.decrementAndGet();

			if (c <= 0) {
				synchronized (this) {
					this.notify();
				}
			}
		}
	}

	public long processingRequestCount() {
		return count.get();
	}

	@Override
	public String shutdownName() {
		return "Http-Processing-Request-Count-Filter";
	}

	/**
	 * 等待足够的时间处理完毕来自web接口的请求或超时
	 */
	@Override
	public void shutdown() {
		closed = true;

		if (count.get() > 0) {
			synchronized (this) {
				try {
					log.info("gracefully shutdown max wait ms:{}", maxWaitMillisOnShutdown);

					this.wait(maxWaitMillisOnShutdown);
				} catch (InterruptedException ignore) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}
}