package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public abstract class ClientRemoteException extends RemoteException {

	private static final long serialVersionUID = 1L;

	public ClientRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientRemoteException(String message) {
		super(message);
	}

	public ClientRemoteException(Throwable cause) {
		super(cause);
	}

}