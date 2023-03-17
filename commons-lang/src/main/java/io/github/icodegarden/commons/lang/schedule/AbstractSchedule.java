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

	private long scheduleTimes;

	private final AtomicBoolean started = new AtomicBoolean(false);
	private final AtomicBoolean closed = new AtomicBoolean(false);

	private ScheduledFuture<?> future;

	@Override
	public boolean scheduleWithFixedDelay(long initialDelayMillis, long scheduleMillis) {
		if (started.compareAndSet(false, true)) {
			future = scheduleThreadPool.scheduleWithFixedDelay(() -> {
				scheduling();
			}, initialDelayMillis, scheduleMillis, TimeUnit.MILLISECONDS);

			return true;
		}
		return false;
	}

	@Override
	public boolean scheduleAtFixedRate(long initialDelayMillis, long scheduleMillis) {
		if (started.compareAndSet(false, true)) {
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

		if (started.compareAndSet(false, true)) {

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
				if (log.isInfoEnabled()) {
					log.info("{} schedule run, scheduleTimes:{}", this.getClass().getSimpleName(), scheduleTimes);
				}

				if (isClosed()) {
					log.info("{} schedule was closed, stop", this.getClass().getSimpleName());
					/**
					 * 如果已关闭，终止执行
					 */
					return;
				}
				doSchedule();
			} catch (Throwable e) {
				log.error("ex on {}", AbstractSchedule.this.getClass().getSimpleName(), e);
			} finally {
				scheduleTimes++;
			}
		}
	}

	protected abstract void doSchedule() throws Throwable;

	@Override
	public boolean isClosed() {
		return closed.get();
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