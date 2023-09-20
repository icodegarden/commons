package io.github.icodegarden.commons.nio.task;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ScheduleCancelableRunnable implements Runnable {
	private static final Logger log = LoggerFactory.getLogger(ScheduleCancelableRunnable.class);

	private final String name;
	private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
	private ScheduledFuture<?> scheduledFuture;

	public ScheduleCancelableRunnable(String name, ScheduledThreadPoolExecutor scheduledThreadPoolExecutor) {
		this.name = name;
		this.scheduledThreadPoolExecutor = scheduledThreadPoolExecutor;
	}

	public void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit timeUnit) {
		if(!scheduledThreadPoolExecutor.isShutdown()) {
			scheduledFuture = scheduledThreadPoolExecutor.scheduleWithFixedDelay(this, initialDelay, delay, timeUnit);
		}
	}
	
	public void scheduleAtFixedRate(long initialDelay, long delay, TimeUnit timeUnit) {
		if(!scheduledThreadPoolExecutor.isShutdown()) {
			scheduledFuture = scheduledThreadPoolExecutor.scheduleAtFixedRate(this, initialDelay, delay, timeUnit);
		}
	}

	public void cancel() {
		boolean cancel = scheduledFuture.cancel(true);
		if (log.isDebugEnabled()) {
			log.debug("cancel schedule:{} result:{}", name, cancel);
		}
	}
}