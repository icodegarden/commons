package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientLimitedErrorCodeException extends ClientErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final String CODE = "40005";
	public static final String MSG = "Call Limited";

	public ClientLimitedErrorCodeException(SubPair subPair) {
		super(CODE, MSG, subPair.getSub_code(), subPair.getSub_msg());
	}

	public ClientLimitedErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}

	@Override
	public int httpStatus() {
		return 403;
	}

	public enum SubPair {
		/**
		 * 整体限流
		 */
		APP_CALL_LIMITED("client.app-call-limited", "Limited:app-call"),
		/**
		 * 接口限流
		 */
		METHOD_CALL_LIMITED("client.method-call-limited", "Limited:method-call"),;

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
