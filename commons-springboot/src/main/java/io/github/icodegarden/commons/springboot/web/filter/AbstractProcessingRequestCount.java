package io.github.icodegarden.commons.springboot.web.filter;

import java.util.concurrent.atomic.AtomicLong;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.springboot.endpoint.ReadinessEndpoint;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class AbstractProcessingRequestCount implements GracefullyShutdown {

	protected AtomicLong count = new AtomicLong(0);
	protected volatile boolean closed;
	protected final int gracefullyShutdownOrder;
	/**
	 * 服务列表刷新间隔
	 */
	protected final long instanceRefreshIntervalMs;
	/**
	 * 等待Processing处理完毕的时间
	 */
	protected long maxProcessingWaitMs = 10000;

	public AbstractProcessingRequestCount(int gracefullyShutdownOrder, long instanceRefreshIntervalMs) {
		this.gracefullyShutdownOrder = gracefullyShutdownOrder;
		this.instanceRefreshIntervalMs = instanceRefreshIntervalMs;
	}

	public void setMaxProcessingWaitMs(long maxProcessingWaitMs) {
		this.maxProcessingWaitMs = maxProcessingWaitMs;
	}

	public long processingRequestCount() {
		return count.get();
	}

	/**
	 * 等待足够的时间处理完毕来自web接口的请求或超时<br>
	 * 因为先进行 服务注销，因此一段时间后不会再有新的请求进来
	 */
	@Override
	public void shutdown() {
		boolean readinessEndpointClosed = false;
		try {
			ReadinessEndpoint readinessEndpoint = SpringContext.getApplicationContext()
					.getBean(ReadinessEndpoint.class);
			readinessEndpointClosed = readinessEndpoint.isClosed();
		} catch (Exception ignore) {
			log.warn("", ignore);
		}

		/**
		 * 如果之前已有ReadinessEndpoint的shutdown，说明这是接入层服务，这里无需等待实例刷新
		 */
		if (readinessEndpointClosed) {
			log.info("gracefully shutdown wait instanceRefresh skip by readinessEndpointClosed");
		} else {
			log.info("gracefully shutdown wait instanceRefresh ms:{}", instanceRefreshIntervalMs);
			try {
				Thread.sleep(instanceRefreshIntervalMs);
			} catch (InterruptedException ignore) {
			}
		}

//		if (count.get() > 0) {
//			synchronized (this) {
//				try {
//					log.info("gracefully shutdown max wait ms:{}", maxProcessingWaitMs);
//
//					this.wait(maxProcessingWaitMs);
//				} catch (InterruptedException ignore) {
//					Thread.currentThread().interrupt();
//				}
//			}
//		}

		/**
		 * 相比上面的方式不用在 finally中this.notify(); 效率好一点点
		 */
		long waitMs = 0;
		int sleepMs = 1000;
		while (count.get() > 0 && waitMs++ < maxProcessingWaitMs) {
			try {
				Thread.sleep(sleepMs);
			} catch (InterruptedException ignore) {
			}
		}

		closed = true;
	}

	@Override
	public int shutdownOrder() {
		return gracefullyShutdownOrder;
	}
}