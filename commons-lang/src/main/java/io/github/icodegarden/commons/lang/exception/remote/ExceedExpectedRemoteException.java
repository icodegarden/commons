package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public class ExceedExpectedRemoteException extends RemoteException {

	private static final long serialVersionUID = 1L;

	public ExceedExpectedRemoteException(String message) {
		super("Exceed Expected-" + message);
	}

	public ExceedExpectedRemoteException(Throwable cause) {
		super("Exceed Expected", cause);
	}

	public ExceedExpectedRemoteException(String message, Throwable cause) {
		super("Exceed Expected-" + message, cause);
	}

}