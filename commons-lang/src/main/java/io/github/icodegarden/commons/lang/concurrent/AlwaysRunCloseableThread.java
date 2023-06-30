package io.github.icodegarden.commons.lang.concurrent;

import io.github.icodegarden.commons.lang.util.LogUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public abstract class AlwaysRunCloseableThread extends CloseableThread {

	private volatile boolean closed;
	private Runnable target;

	public AlwaysRunCloseableThread() {
		super();
	}

	public AlwaysRunCloseableThread(Runnable target, String name) {
		super(target, name);
	}

	public AlwaysRunCloseableThread(Runnable target) {
		super(target);
	}

	public AlwaysRunCloseableThread(String name) {
		super(name);
	}

	@Override
	public final void run() {
		synchronized (this) {// 关闭等待用
			long loop = 0;
			while (!closed) {
				if (target != null) {
					super.run();
				} else {
					doRun(loop);// from 0
				}
				if (loop == Long.MAX_VALUE) {
					loop = 0;
				}
				loop++;
			}
			doClose();
		}
	}

	@Override
	public final void close() {
		LogUtils.infoIfEnabled(log, () -> log.info("{} start close.", getName()));
		
		closed = true;
		
		/**
		 * 中断执行中的线程，因为线程可能处于阻塞中，如果不中断可能无法退出
		 */
		this.interrupt();
		
		/**
		 * 使用synchronized保障如果任务正在处理中，则等待任务处理完毕
		 */
		synchronized (this) {
		}
		
		LogUtils.infoIfEnabled(log, () -> log.info("{} complete close.", getName()));
	}

	/**
	 * 当Runnable没有时，必须实现
	 * 
	 * @param loop
	 */
	protected void doRun(long loop) {
		throw new RuntimeException("method must override on Runnable not given");
	}

	/**
	 * close时需要处理的逻辑，可以不做任何处理
	 */
	protected void doClose() {
	}
}
