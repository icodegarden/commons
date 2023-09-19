package io.github.icodegarden.commons.lang.concurrent.registry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class RegistryException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public RegistryException(String message, Throwable cause) {
		super(message, cause);
	}

	public RegistryException(Throwable cause) {
		super(cause);
	}

	public RegistryException(String message) {
		super(message);
	}

}
