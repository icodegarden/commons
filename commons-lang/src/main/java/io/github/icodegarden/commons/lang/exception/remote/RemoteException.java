package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public abstract class RemoteException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public RemoteException(String message) {
		super(message);
	}

	public RemoteException(Throwable cause) {
		super(cause);
	}

}