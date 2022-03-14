package io.github.icodegarden.commons.exchange.http;

import java.util.HashMap;

import io.github.icodegarden.commons.lang.exception.remote.ClientInvalidRemoteException;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * @author Fangfang.Xu
 */
public class HttpClientRemoteException extends ClientInvalidRemoteException {

	private static final long serialVersionUID = 1L;

	private final int httpStatus;

	public int getHttpStatus() {
		return httpStatus;
	}

	public HttpClientRemoteException(String message, int httpStatus, HttpHeaders headers) {
		super(buildMessage(message, httpStatus, headers), Integer.toString(httpStatus));
		this.httpStatus = httpStatus;
	}

	public HttpClientRemoteException(String message, int httpStatus, HttpHeaders headers, Throwable cause) {
		super(buildMessage(message, httpStatus, headers), Integer.toString(httpStatus), cause);
		this.httpStatus = httpStatus;
	}

	private static String buildMessage(String message, int httpStatus, HttpHeaders headers) {
		HashMap<String, Object> hashMap = new HashMap<String, Object>(4, 1);
		hashMap.put("message", message);
		hashMap.put("httpStatus", httpStatus);
		if(headers != null) {
			hashMap.put("httpHeaders", headers);			
		}

		return JsonUtils.serialize(hashMap);
	}
}