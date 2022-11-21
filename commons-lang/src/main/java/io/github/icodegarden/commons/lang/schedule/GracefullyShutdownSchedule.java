package io.github.icodegarden.commons.lang.schedule;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.icodegarden.commons.lang.endpoint.GracefullyShutdown;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class GracefullyShutdownSchedule implements GracefullyShutdown, Closeable {

	private final ScheduledThreadPoolExecutor scheduleThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool(GracefullyShutdownSchedule.this.getClass().getSimpleName());
	{
		scheduleThreadPool.setRemoveOnCancelPolicy(true);
	}

	protected long loop;
	private long logmod = 100;

	protected final AtomicBoolean closed = new AtomicBoolean(true);

	private ScheduledFuture<?> future;

	public boolean start(long initialDelayMillis, long scheduleMillis) {
		if (closed.compareAndSet(true, false)) {
			future = scheduleThreadPool.scheduleWithFixedDelay(() -> {
				synchronized (this) {// 关闭等待用
					try {
						if (log.isInfoEnabled() && allowLoopLog()) {
							log.info("{} schedule run, loop:{}", this.getClass().getSimpleName(), loop);
						}
						loop++;

						if (closed.get()) {
							log.info("{} schedule was closed, stop", this.getClass().getSimpleName());
							/**
							 * 如果已关闭，终止执行
							 */
							return;
						}
						doSchedule();
					} catch (Throwable e) {
						log.warn("ex on {}", GracefullyShutdownSchedule.this.getClass().getSimpleName(), e);
					}
				}
			}, initialDelayMillis, scheduleMillis, TimeUnit.MILLISECONDS);

			return true;
		}
		return false;
	}

	protected abstract void doSchedule();

	public void setLogmod(long logmod) {
		this.logmod = logmod;
	}

	protected boolean allowLoopLog() {
		return loop % logmod == 0;
	}

	@Override
	public String shutdownName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * 阻塞直到处理完毕，这不会阻塞很久
	 */
	@Override
	public void shutdown() {
		if (future != null) {
			future.cancel(true);
		}
		closed.set(true);

		/**
		 * 使用synchronized保障如果任务正在处理中，则等待任务处理完毕
		 */
		synchronized (this) {
		}
	}

	@Override
	public void close() throws IOException {
		shutdown();
	}
}