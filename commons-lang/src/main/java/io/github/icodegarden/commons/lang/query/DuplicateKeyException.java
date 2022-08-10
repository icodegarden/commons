package io.github.icodegarden.commons.lang.query;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class DuplicateKeyException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DuplicateKeyException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateKeyException(String message) {
		super(message);
	}

}
