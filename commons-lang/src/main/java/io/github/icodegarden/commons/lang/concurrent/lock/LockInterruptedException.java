package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LockInterruptedException extends LockException {
	private static final long serialVersionUID = 1L;

	public LockInterruptedException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockInterruptedException(Throwable cause) {
		super(cause);
	}
}