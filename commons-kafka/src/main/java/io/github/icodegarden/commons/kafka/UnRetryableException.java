package io.github.icodegarden.commons.kafka;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class UnRetryableException extends Exception {

	private static final long serialVersionUID = 1L;

	public UnRetryableException() {
		super();
	}

	public UnRetryableException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnRetryableException(String message) {
		super(message);
	}

	public UnRetryableException(Throwable cause) {
		super(cause);
	}
}