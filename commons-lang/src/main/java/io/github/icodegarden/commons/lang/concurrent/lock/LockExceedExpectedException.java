package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LockExceedExpectedException extends LockException {
	private static final long serialVersionUID = 1L;

	public LockExceedExpectedException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockExceedExpectedException(Throwable cause) {
		super(cause);
	}
}