package io.github.icodegarden.commons.lang.schedule;

import io.github.icodegarden.commons.lang.concurrent.lock.DistributedLock;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * 分批执行的调度任务
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class LockSupportForeachSchedule extends LockSupportSchedule {

	private final long timeoutMillis;

	public LockSupportForeachSchedule(DistributedLock lock) {
		this(lock, Long.MAX_VALUE);
	}

	/**
	 * @param timeoutMillis 任务超时时间，超过将中断执行
	 */
	public LockSupportForeachSchedule(DistributedLock lock, long timeoutMillis) {
		super(lock);
		this.timeoutMillis = timeoutMillis;
	}

	@Override
	protected void doScheduleAfterLocked() throws Throwable {
		log.info("{} start foreach schedule", this.getClass().getSimpleName());

		preForeach();

		long loop = 0;
		long start = System.currentTimeMillis();

		for (;;) {
			if (isClosed()) {
				log.warn("{} was closed, loop:{}, interrupt foreach", this.getClass().getSimpleName(), loop);
				/**
				 * 如果在执行过程中关闭，终止执行
				 */
				break;
			}

			if (!lock.isAcquired()) {
				log.warn("{} lock was lost, loop:{}, interrupt foreach", this.getClass().getSimpleName(), loop);
				/**
				 * 每轮检查锁是否还持有，因为zk的锁可能由于网络问题session超时而失去持有被其他进程获取锁
				 */
				break;
			}

			if (System.currentTimeMillis() - start > timeoutMillis) {
				log.warn("{} do foreach was timeout:{}, loop:{}, interrupt foreach", this.getClass().getSimpleName(),
						timeoutMillis, loop);
				break;
			}

			DoForeachResult result = doForeach(loop);

			if (result.isCompletedAll()) {
				log.info("{} completed foreach, loop:{}, total cost:", this.getClass().getSimpleName(), loop,
						(System.currentTimeMillis() - start));
				break;
			}
		}

		postForeach(loop);
	}

	/**
	 * 整个Foreach开始前
	 */
	protected void preForeach() throws Throwable {
		// 有需要就覆盖
	}

	/**
	 * @return 任务是否已全部完成，全部完成后将不再触发该方法
	 */
	protected abstract DoForeachResult doForeach(long loop) throws Throwable;

	/**
	 * 整个Foreach成功结束后
	 */
	protected void postForeach(long loop) throws Throwable {
		// 有需要就覆盖
	}

	@Getter
	@Setter
	@ToString
	public static class DoForeachResult {
		private final boolean completedAll;

		public DoForeachResult(boolean completedAll) {
			this.completedAll = completedAll;
		}
	}
}