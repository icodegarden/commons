package io.github.icodegarden.commons.lang.serialization;

/**
 * @author Fangfang.Xu
 */
public class SerializationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

}