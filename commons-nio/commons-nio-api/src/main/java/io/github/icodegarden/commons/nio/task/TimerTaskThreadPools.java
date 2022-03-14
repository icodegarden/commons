package io.github.icodegarden.commons.nio.task;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class TimerTaskThreadPools {

	public static final ScheduledThreadPoolExecutor SCHEDULED_THREADPOOLS = new ScheduledThreadPoolExecutor(2,
			new ThreadFactory() {
				protected final AtomicInteger mThreadNum = new AtomicInteger(1);

				@Override
				public Thread newThread(Runnable runnable) {
					String name = "TimerTaskThreadPools-" + mThreadNum.getAndIncrement();
					Thread ret = new Thread(runnable, name);
					return ret;
				}
			});
	static {
		SCHEDULED_THREADPOOLS.setRemoveOnCancelPolicy(true);
	}
}
