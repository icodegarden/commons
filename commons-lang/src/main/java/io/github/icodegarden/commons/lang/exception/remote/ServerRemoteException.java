package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public abstract class ServerRemoteException extends RemoteException {

	private static final long serialVersionUID = 1L;

	public ServerRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServerRemoteException(String message) {
		super(message);
	}

	public ServerRemoteException(Throwable cause) {
		super(cause);
	}

}