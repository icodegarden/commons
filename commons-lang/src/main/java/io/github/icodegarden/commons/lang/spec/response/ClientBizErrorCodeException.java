package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientBizErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final String CODE = "40004";
	public static final String MSG = "Biz Failed";
	private final int httpStatus;

	public ClientBizErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
		httpStatus = 403;
	}

	public ClientBizErrorCodeException(SubCode subCode, String sub_msg) {
		super(CODE, MSG, subCode.getSub_code(), sub_msg);
		this.httpStatus = subCode.getHttp_status();
	}

	@Override
	public int httpStatus() {
		return httpStatus;
	}

	public enum SubCode {
		FORBIDDEN("client.operation-forbidden", 403), 
		NOT_FOUND("client.resource-not-found", 404),
		CONFLICT("client.resource-conflict", 409),;

		private final String sub_code;
		private final int http_status;

		private SubCode(String sub_code, int http_status) {
			this.sub_code = sub_code;
			this.http_status = http_status;
		}

		public String getSub_code() {
			return sub_code;
		}

		public int getHttp_status() {
			return http_status;
		}
	}
}
