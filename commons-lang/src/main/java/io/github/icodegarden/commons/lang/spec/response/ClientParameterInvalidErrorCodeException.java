package io.github.icodegarden.commons.lang.spec.response;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientParameterInvalidErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final List<String> KEYWORDS = Arrays.asList("Invalid");
	
	public static final String CODE = "40002";
	public static final String MSG = "Invalid Parameter";

	public ClientParameterInvalidErrorCodeException(SubPair subPair) {
		super(CODE, MSG, subPair.getSub_code(), subPair.getSub_msg());
	}

	public ClientParameterInvalidErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 400;
	}

	public enum SubPair {
		INVALID_PARAMETER("client.invalid-parameter", "Invalid:parameter"),
		UPLOAD_FAIL("client.upload-fail", "upload-fail"),
		INVALID_FILE_EXTENSION("client.invalid-file-extension", "Invalid:file-extension"),
		INVALID_FILE_SIZE("client.invalid-file-size", "Invalid:file-size"),
		INVALID_METHOD("client.invalid-method", "Invalid:method"),
		INVALID_FORMAT("client.invalid-format", "Invalid:format"),
		INVALID_SIGNATURE_TYPE("client.invalid-signature-type", "Invalid:sign_type"),
		INVALID_SIGNATURE("client.invalid-signature", "Invalid:sign"),
		INVALID_APP_ID("client.invalid-app-id", "Invalid:app-id"),
		INVALID_TIMESTAMP("client.invalid-timestamp", "Invalid:timestamp"),
		INVALID_CHARSET("client.invalid-charset", "Invalid:charset"),
		INVALID_REQUEST_ID("client.invalid-request-id", "Invalid:request_id"),;

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
