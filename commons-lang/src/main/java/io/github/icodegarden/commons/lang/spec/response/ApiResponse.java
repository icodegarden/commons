package io.github.icodegarden.commons.lang.spec.response;

import java.util.Objects;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ApiResponse {

	public static final String CODE_SUCCESS = "10000";
	public static final String MSG_SUCCESS = "Success";

	private String code;
	private String msg;
	private String sub_code;
	private String sub_msg;

	public boolean isSuccess() {
		return CODE_SUCCESS.equals(code);
	}

	public ErrorCodeException toErrorCodeException() {
		if (ClientBizErrorCodeException.CODE.equals(code)) {
			if (ClientBizErrorCodeException.SubCode.FORBIDDEN.getSub_code().equals(sub_code)) {
				return new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.FORBIDDEN, sub_msg);
			} else if (ClientBizErrorCodeException.SubCode.NOT_FOUND.getSub_code().equals(sub_code)) {
				return new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.NOT_FOUND, sub_msg);
			} else if (ClientBizErrorCodeException.SubCode.CONFLICT.getSub_code().equals(sub_code)) {
				return new ClientBizErrorCodeException(ClientBizErrorCodeException.SubCode.CONFLICT, sub_msg);
			} else {
				return new ClientBizErrorCodeException(sub_code, sub_msg);
			}
		} else if (ClientLimitedErrorCodeException.CODE.equals(code)) {
			return new ClientLimitedErrorCodeException(sub_code, sub_msg);
		} else if (ClientParameterInvalidErrorCodeException.CODE.equals(code)) {
			return new ClientParameterInvalidErrorCodeException(sub_code, sub_msg);
		} else if (ClientParameterMissingErrorCodeException.CODE.equals(code)) {
			return new ClientParameterMissingErrorCodeException(sub_code, sub_msg);
		} else if (ClientPermissionErrorCodeException.CODE.equals(code)) {
			return new ClientPermissionErrorCodeException(sub_code, sub_msg);
		} else if (ServerErrorCodeException.CODE.equals(code)) {
			return new ServerErrorCodeException(sub_code, sub_msg);
		} else {
			return new ErrorCodeExceptionImpl();
		}
	}

	private class ErrorCodeExceptionImpl extends ErrorCodeException {
		private static final long serialVersionUID = 1L;

		public ErrorCodeExceptionImpl() {
			super(code, msg, sub_code, sub_msg);
		}

		@Override
		public int httpStatus() {
			return -1;
		}
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getSub_code() {
		return sub_code;
	}

	public void setSub_code(String sub_code) {
		this.sub_code = sub_code;
	}

	public String getSub_msg() {
		return sub_msg;
	}

	public void setSub_msg(String sub_msg) {
		this.sub_msg = sub_msg;
	}

	@Override
	public int hashCode() {
		return Objects.hash(code, msg, sub_code, sub_msg);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApiResponse other = (ApiResponse) obj;
		return Objects.equals(code, other.code) && Objects.equals(msg, other.msg)
				&& Objects.equals(sub_code, other.sub_code) && Objects.equals(sub_msg, other.sub_msg);
	}

	@Override
	public String toString() {
		return "ApiResponse [code=" + code + ", msg=" + msg + ", sub_code=" + sub_code + ", sub_msg=" + sub_msg + "]";
	}

}
