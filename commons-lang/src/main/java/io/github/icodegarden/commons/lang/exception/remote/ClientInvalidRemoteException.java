package io.github.icodegarden.commons.lang.exception.remote;

/**
 * 客户端原因，如参数不符、认证不通过、请求的接口不允许、被服务端限流 等
 * 
 * @author Fangfang.Xu
 */
public class ClientInvalidRemoteException extends ClientRemoteException {

	private static final long serialVersionUID = 1L;

	private final String statusCode;

	public ClientInvalidRemoteException(String message, String statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	public ClientInvalidRemoteException(String message, String statusCode, Throwable cause) {
		super(message, cause);
		this.statusCode = statusCode;
	}

	public String getStatusCode() {
		return statusCode;
	}
}