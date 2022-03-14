package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public class ClientClosedRemoteException extends ClientRemoteException {

	private static final long serialVersionUID = 1L;

	public ClientClosedRemoteException(String message) {
		super(message);
	}
	
	public ClientClosedRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

}