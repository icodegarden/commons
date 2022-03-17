package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientParameterMissingErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final String CODE = "40001";
	public static final String MSG = "Missing Required Parameter";

	public ClientParameterMissingErrorCodeException(SubPair subPair) {
		super(CODE, MSG, subPair.getSub_code(), subPair.getSub_msg());
	}

	public ClientParameterMissingErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 400;
	}

	public enum SubPair {
		MISSING_METHOD("client.missing-method", "Missing:method"),
		MISSING_SIGNATURE("client.missing-signature", "Missing:sign"),
		MISSING_SIGNATURE_TYPE("client.missing-signature-type", "Missing:sign_type"),
		MISSING_SIGNATURE_KEY("client.missing-signature_key", "Missing:key"),
		MISSING_APP_ID("client.missing-app-id", "Missing:app_id"),
		MISSING_TIMESTAMP("client.missing-timestamp", "Missing:timestamp"),
		MISSING_VERSION("client.missing-version", "Missing:version"),
		MISSING_REQUEST_ID("client.missing-request-id", "Missing:request_id"),
		/**
		 * 其他参数
		 */
		MISSING_PARAMETER("client.missing-parameter", "Missing:parameter"),;

		private final String sub_code;
		private final String sub_msg;

		private SubPair(String sub_code, String sub_msg) {
			this.sub_code = sub_code;
			this.sub_msg = sub_msg;
		}

		public String getSub_code() {
			return sub_code;
		}

		public String getSub_msg() {
			return sub_msg;
		}
	}
}
