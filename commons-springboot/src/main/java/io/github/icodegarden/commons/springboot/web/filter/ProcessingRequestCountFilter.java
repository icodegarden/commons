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
 * 可以统计处理中的web请求数，可以优雅停机<br>
 * 这个类的shutdownOrder数值应该配置的比 服务注销 的GracefullyShutdown数值大
 * 
 * @author Fangfang.Xu
 *
 */
public class ProcessingRequestCountFilter implements Filter, GracefullyShutdown {

	private static final Logger log = LoggerFactory.getLogger(ProcessingRequestCountFilter.class);

	private AtomicLong count = new AtomicLong(0);
	private volatile boolean closed;
	private final int gracefullyShutdownOrder;
	/**
	 * 服务列表刷新间隔
	 */
	private final long instanceRefreshIntervalMs;
	/**
	 * 等待Processing处理完毕的时间
	 */
	private long maxProcessingWaitMs = 10000;

	public ProcessingRequestCountFilter(int gracefullyShutdownOrder, long instanceRefreshIntervalMs) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
		this.instanceRefreshIntervalMs = instanceRefreshIntervalMs;
	}

	public void setMaxProcessingWaitMs(long maxProcessingWaitMs) {
		this.maxProcessingWaitMs = maxProcessingWaitMs;
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
		return "Processing-Request-Count-Filter";
	}

	/**
	 * 等待足够的时间处理完毕来自web接口的请求或超时<br>
	 * 因为先进行 服务注销，因此一段时间后不会再有新的请求进来
	 */
	@Override
	public void shutdown() {
		log.info("gracefully shutdown wait instanceRefresh ms:{}", instanceRefreshIntervalMs);
		try {
			Thread.sleep(instanceRefreshIntervalMs);
		} catch (InterruptedException ignore) {
		}

		if (count.get() > 0) {
			synchronized (this) {
				try {
					log.info("gracefully shutdown max wait ms:{}", maxProcessingWaitMs);

					this.wait(maxProcessingWaitMs);
				} catch (InterruptedException ignore) {
					Thread.currentThread().interrupt();
				}
			}
		}

		closed = true;
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}
}