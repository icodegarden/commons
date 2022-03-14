package io.github.icodegarden.commons.lang.concurrent;

import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
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

	@Override
	public final void close() throws IOException {
		closed = true;
	}

	/**
	 * 当Runnable没有时，必须实现
	 * @param loop
	 */
	protected void doRun(long loop) {
		throw new RuntimeException("method must override on Runnable not given");
	}

	/**
	 * close时需要处理的逻辑，可以不做任何处理
	 */
	protected abstract void doClose();
}
