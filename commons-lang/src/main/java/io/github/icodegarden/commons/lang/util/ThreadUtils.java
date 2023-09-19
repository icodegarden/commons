package io.github.icodegarden.commons.lang.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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

	/**
	 * 固定数量线程池，不会回收<br>
	 * 任务处理线程不足才会进队列
	 */
	public static ThreadPoolExecutor newFixedThreadPool(int poolSize, int queueSize, String threadPrefix) {
		return new ThreadPoolExecutor(//
				poolSize, //
				poolSize, //
				0, //
				TimeUnit.SECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix)//
		);
	}

	/**
	 * 固定数量线程池，不会回收<br>
	 * 任务处理线程不足才会进队列
	 */
	public static ThreadPoolExecutor newFixedThreadPool(int poolSize, int queueSize, String threadPrefix,
			RejectedExecutionHandler rejectedExecutionHandler) {
		return new ThreadPoolExecutor(//
				poolSize, //
				poolSize, //
				0, //
				TimeUnit.SECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix), //
				rejectedExecutionHandler//
		);
	}

	/**
	 * 弹性数量线程池，会回收<br>
	 * 任务core处理线程不足就会进队列，队列满后才会增长弹性线程
	 */
	public static ThreadPoolExecutor newCachedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveMs,
			int queueSize, String threadPrefix) {
		return new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				keepAliveMs, //
				TimeUnit.MILLISECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix)//
		);
	}

	/**
	 * 弹性数量线程池，会回收<br>
	 * 任务core处理线程不足就会进队列，队列满后才会增长弹性线程
	 */
	public static ThreadPoolExecutor newCachedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveMs,
			int queueSize, String threadPrefix, RejectedExecutionHandler rejectedExecutionHandler) {
		return new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				keepAliveMs, //
				TimeUnit.MILLISECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix), //
				rejectedExecutionHandler//
		);
	}

	/**
	 * 饥饿数量线程池，会回收<br>
	 * 任务core处理线程不足就会增长弹性线程，弹性线程不足才会进队列
	 */
	public static ThreadPoolExecutor newEagerThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveMs,
			int queueSize, String threadPrefix) {
		TaskQueue<Runnable> taskQueue = new TaskQueue<Runnable>(queueSize <= 0 ? 1 : queueSize);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				keepAliveMs, //
				TimeUnit.MILLISECONDS, //
				taskQueue, //
				new NamedThreadFactory(threadPrefix)//
		);
		taskQueue.setExecutor(executor);
		return executor;
	}

	/**
	 * 饥饿数量线程池，会回收<br>
	 * 任务core处理线程不足就会增长弹性线程，弹性线程不足才会进队列
	 */
	public static ThreadPoolExecutor newEagerThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveMs,
			int queueSize, String threadPrefix, RejectedExecutionHandler rejectedExecutionHandler) {
		TaskQueue<Runnable> taskQueue = new TaskQueue<Runnable>(queueSize <= 0 ? 1 : queueSize);
		ThreadPoolExecutor executor = new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				keepAliveMs, //
				TimeUnit.MILLISECONDS, //
				taskQueue, //
				new NamedThreadFactory(threadPrefix), //
				rejectedExecutionHandler//
		);
		taskQueue.setExecutor(executor);
		return executor;
	}

	/**
	 * 限制数量线程池，不会回收<br>
	 * 任务core处理线程不足就会进队列，队列满后才会增长弹性线程
	 */
	public static ThreadPoolExecutor newLimitedThreadPool(int corePoolSize, int maximumPoolSize, int queueSize,
			String threadPrefix) {
		return new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				Long.MAX_VALUE, //
				TimeUnit.MILLISECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix)//
		);
	}

	/**
	 * 限制数量线程池，不会回收<br>
	 * 任务core处理线程不足就会进队列，队列满后才会增长弹性线程
	 */
	public static ThreadPoolExecutor newLimitedThreadPool(int corePoolSize, int maximumPoolSize, int queueSize,
			String threadPrefix, RejectedExecutionHandler rejectedExecutionHandler) {
		return new ThreadPoolExecutor(//
				corePoolSize, //
				maximumPoolSize, //
				Long.MAX_VALUE, //
				TimeUnit.MILLISECONDS, //
				queueSize == 0 ? new SynchronousQueue<Runnable>()
						: (queueSize < 0 ? new LinkedBlockingQueue<Runnable>()
								: new LinkedBlockingQueue<Runnable>(queueSize)), //
				new NamedThreadFactory(threadPrefix), //
				rejectedExecutionHandler//
		);
	}

	public static ScheduledThreadPoolExecutor newSingleScheduledThreadPool(String threadPrefix) {
		return newScheduledThreadPool(1, threadPrefix);
	}

	public static ScheduledThreadPoolExecutor newLightResourceScheduledThreadPool(String threadPrefix) {
		return newScheduledThreadPool(Math.max(Runtime.getRuntime().availableProcessors() / 2 + 1, 4), threadPrefix);
	}

	public static ScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, String threadPrefix) {
		return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory(threadPrefix));
	}

	/**
	 * copy from org.apache.dubbo.common.threadpool.support.eager.TaskQueue<R>
	 */
	private static class TaskQueue<R extends Runnable> extends LinkedBlockingQueue<Runnable> {

		private static final long serialVersionUID = -2635853580887179627L;

		private ThreadPoolExecutor executor;

		public TaskQueue(int capacity) {
			super(capacity);
		}

		public void setExecutor(ThreadPoolExecutor exec) {
			executor = exec;
		}

		@Override
		public boolean offer(Runnable runnable) {
			if (executor == null) {
				throw new RejectedExecutionException("The task queue does not have executor!");
			}

			int currentPoolThreadSize = executor.getPoolSize();
			// have free worker. put task into queue to let the worker deal with task.
			if (executor.getActiveCount() < currentPoolThreadSize) {
				return super.offer(runnable);
			}

			// return false to let executor create new worker.
			if (currentPoolThreadSize < executor.getMaximumPoolSize()) {
				return false;
			}

			// currentPoolThreadSize >= max
			return super.offer(runnable);
		}
	}
}