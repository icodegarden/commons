package io.github.icodegarden.commons.lang.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class ThreadUtils {

	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			if (log.isWarnEnabled()) {
				log.warn("sleep Interrupted", e);
			}
		}
	}

	public static void sleepInterruptibly(long millis) throws InterruptedException {
		Thread.sleep(millis);
	}

//	public static final ScheduledExecutorService LIGHT_RESOURCE_SINGLE_THREAD_SCHEDULER = Executors
//			.newSingleThreadScheduledExecutor(new NamedThreadFactory("Light-Resource-Single-Thread-Scheduler"));
//
//	public static final ScheduledExecutorService LIGHT_RESOURCE_THREAD_SCHEDULER = Executors.newScheduledThreadPool(
//			Math.max(Runtime.getRuntime().availableProcessors() / 2 + 1, 4),
//			new NamedThreadFactory("Light-Resource-Thread-Scheduler"));

	public static ScheduledThreadPoolExecutor newSingleScheduledThreadPool(String threadPrefix) {
		return newScheduledThreadPool(1, threadPrefix);
	}

	public static ScheduledThreadPoolExecutor newLightResourceScheduledThreadPool(String threadPrefix) {
		return newScheduledThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2 + 1, 4), threadPrefix);
	}

	public static ScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, String threadPrefix) {
		return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(threadPrefix));
	}
}