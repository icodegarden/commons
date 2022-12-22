package io.github.icodegarden.commons.lang.schedule;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.util.CronUtils;
import io.github.icodegarden.commons.lang.util.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class AbstractSchedule implements Schedule {

	private final ScheduledThreadPoolExecutor scheduleThreadPool = ThreadPoolUtils
			.newSingleScheduledThreadPool(AbstractSchedule.this.getClass().getSimpleName());
	{
		scheduleThreadPool.setRemoveOnCancelPolicy(true);
	}

	private long loop;

	private long logmod = 100;

	private final AtomicBoolean closed = new AtomicBoolean(true);

	private ScheduledFuture<?> future;

	@Override
	public boolean scheduleWithFixedDelay(long initialDelayMillis, long scheduleMillis) {
		if (closed.compareAndSet(true, false)) {
			future = scheduleThreadPool.scheduleWithFixedDelay(() -> {
				scheduling();
			}, initialDelayMillis, scheduleMillis, TimeUnit.MILLISECONDS);

			return true;
		}
		return false;
	}

	@Override
	public boolean scheduleAtFixedRate(long initialDelayMillis, long scheduleMillis) {
		if (closed.compareAndSet(true, false)) {
			future = scheduleThreadPool.scheduleAtFixedRate(() -> {
				scheduling();
			}, initialDelayMillis, scheduleMillis, TimeUnit.MILLISECONDS);

			return true;
		}
		return false;
	}

	@Override
	public boolean scheduleWithCron(String cron) {
		Assert.isTrue(CronUtils.isValid(cron), "Invalid:cron");
		
		if (closed.compareAndSet(true, false)) {

			doCron(cron);

			return true;
		}
		return false;
	}

	private void doCron(String cron) {
		future = scheduleThreadPool.schedule(() -> {
			scheduling();

			doCron(cron);
		}, CronUtils.nextDelayMillis(cron), TimeUnit.MILLISECONDS);
	}

	private void scheduling() {
		synchronized (this) {// 关闭等待用
			try {
				if (log.isInfoEnabled() && allowLoopLog()) {
					log.info("{} schedule run, loop:{}", this.getClass().getSimpleName(), loop);
				}

				if (closed.get()) {
					log.info("{} schedule was closed, stop", this.getClass().getSimpleName());
					/**
					 * 如果已关闭，终止执行
					 */
					return;
				}
				doSchedule();
			} catch (Throwable e) {
				log.warn("ex on {}", AbstractSchedule.this.getClass().getSimpleName(), e);
			} finally {
				loop++;
			}
		}
	}

	protected abstract void doSchedule() throws Throwable;

	public void setLogmod(long logmod) {
		this.logmod = logmod;
	}

	protected boolean allowLoopLog() {
		return loop % logmod == 0;
	}

	/**
	 * 阻塞直到处理完毕，这不会阻塞很久
	 */
	@Override
	public void close() {
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
}