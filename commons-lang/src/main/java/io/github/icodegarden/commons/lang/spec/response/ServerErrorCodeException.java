package io.github.icodegarden.commons.lang.spec.response;

import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServerErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final String CODE = "20000";
	public static final String MSG = "Service Currently Unavailable";

	public static void configApplicationName(String applicationName) {
		SystemUtils.Server.configServerName(applicationName);
	}

	private static String getApplicationName() {
		return SystemUtils.Server.getServerName();
	}

	public ServerErrorCodeException(Throwable cause) {
		super(CODE, MSG, String.format("server.%s.unknown-error", getApplicationName()), MSG, cause);
	}

	public ServerErrorCodeException(String error_point, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", getApplicationName(), error_point), MSG, cause);
	}

	public ServerErrorCodeException(String error_point, String sub_msg, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", getApplicationName(), error_point), sub_msg, cause);
	}

	ServerErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 500;
	}
}
