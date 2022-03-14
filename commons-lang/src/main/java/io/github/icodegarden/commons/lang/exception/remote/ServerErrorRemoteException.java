package io.github.icodegarden.commons.lang.exception.remote;

/**
 * @author Fangfang.Xu
 */
public class ServerErrorRemoteException extends ServerRemoteException {

	private static final long serialVersionUID = 1L;

	public ServerErrorRemoteException(String message) {
		super(message);
	}
	
	public ServerErrorRemoteException(String message, Throwable cause) {
		super(message, cause);
	}

}