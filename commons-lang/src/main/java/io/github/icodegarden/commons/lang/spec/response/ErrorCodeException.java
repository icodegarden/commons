package io.github.icodegarden.commons.lang.spec.response;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ErrorCodeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final String code;
	private final String msg;
	private final String sub_code;
	private final String sub_msg;
	private Object reference;

	public ErrorCodeException(String code, String msg, String sub_code, String sub_msg) {
		this.code = code;
		this.msg = msg;
		this.sub_code = sub_code;
		this.sub_msg = sub_msg;
	}

	public ErrorCodeException(String code, String msg, String sub_code, String sub_msg,
			Throwable cause) {
		super(cause);
		this.code = code;
		this.msg = msg;
		this.sub_code = sub_code;
		this.sub_msg = sub_msg;
	}

	public ErrorCodeException(String code, String msg, String sub_code, String sub_msg, Throwable cause,
			Object reference) {
		super(cause);
		this.code = code;
		this.msg = msg;
		this.sub_code = sub_code;
		this.sub_msg = sub_msg;
		this.reference = reference;
	}
	
	public abstract int httpStatus();

	@Override
	public String getMessage() {
		int length = 5;// code:
		length += code != null ? code.length() : 0;
		length += 6;// , msg:
		length += msg != null ? msg.length() : 0;
		length += 11;// , sub_code:
		length += sub_code != null ? sub_code.length() : 0;
		length += 10;// , sub_msg:
		length += sub_msg != null ? sub_msg.length() : 0;
		StringBuilder sb = new StringBuilder(length).append("code:").append(code).append(", msg:").append(msg)
				.append(", sub_code:").append(sub_code).append(", sub_msg:").append(sub_msg);
		return sb.toString();
	}

	public Object getReference() {
		return reference;
	}

	public void setReference(Object reference) {
		this.reference = reference;
	}

	public String getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public String getSub_code() {
		return sub_code;
	}

	public String getSub_msg() {
		return sub_msg;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + ((sub_code == null) ? 0 : sub_code.hashCode());
		result = prime * result + ((sub_msg == null) ? 0 : sub_msg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ErrorCodeException other = (ErrorCodeException) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (msg == null) {
			if (other.msg != null)
				return false;
		} else if (!msg.equals(other.msg))
			return false;
		if (sub_code == null) {
			if (other.sub_code != null)
				return false;
		} else if (!sub_code.equals(other.sub_code))
			return false;
		if (sub_msg == null) {
			if (other.sub_msg != null)
				return false;
		} else if (!sub_msg.equals(other.sub_msg))
			return false;
		return true;
	}

}
