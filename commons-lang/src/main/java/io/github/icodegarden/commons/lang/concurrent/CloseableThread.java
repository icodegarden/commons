package io.github.icodegarden.commons.lang.concurrent;

import java.io.Closeable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CloseableThread extends Thread implements Closeable {

	public CloseableThread() {
		super();
	}

	public CloseableThread(Runnable target, String name) {
		super(target, name);
	}

	public CloseableThread(Runnable target) {
		super(target);
	}

	public CloseableThread(String name) {
		super(name);
	}
	
	public abstract void close();
}
