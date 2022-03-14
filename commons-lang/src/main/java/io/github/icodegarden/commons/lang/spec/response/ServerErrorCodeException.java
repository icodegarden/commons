package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServerErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	private static String applicationName = "NotConfig";

	public static final String CODE = "20000";
	public static final String MSG = "Service Currently Unavailable";

	public static void configApplicationName(String applicationName) {
		ServerErrorCodeException.applicationName = applicationName;
	}

	public ServerErrorCodeException(Throwable cause) {
		super(CODE, MSG, String.format("server.%s.unknown-error", applicationName), MSG, cause);
	}

	public ServerErrorCodeException(String error_point, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", applicationName, error_point), MSG, cause);
	}
	
	public ServerErrorCodeException(String error_point, String sub_msg, Throwable cause) {
		super(CODE, MSG, String.format("server.%s.%s-error", applicationName, error_point), sub_msg, cause);
	}

	ServerErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 500;
	}
}
