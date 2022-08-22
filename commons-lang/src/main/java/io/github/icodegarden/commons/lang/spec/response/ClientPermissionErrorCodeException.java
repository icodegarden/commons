package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientPermissionErrorCodeException extends ErrorCodeException {
	private static final long serialVersionUID = 1L;

	public static final String CODE = "40006";
	public static final String MSG = "Insufficient Permissions";

	public ClientPermissionErrorCodeException(SubPair subPair) {
		super(CODE, MSG, subPair.getSub_code(), subPair.getSub_msg());
	}

	public ClientPermissionErrorCodeException(String sub_code, String sub_msg) {
		super(CODE, MSG, sub_code, sub_msg);
	}
	
	@Override
	public int httpStatus() {
		return 401;
	}

	public enum SubPair {
		INSUFFICIENT_PERMISSIONS("client.insufficient-permissions", "Insufficient:permissions");

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
