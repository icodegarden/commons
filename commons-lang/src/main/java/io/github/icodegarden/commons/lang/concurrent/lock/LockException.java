package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class LockException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public LockException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockException(Throwable cause) {
		super(cause);
	}

	public LockException(String message) {
		super(message);
	}

}
