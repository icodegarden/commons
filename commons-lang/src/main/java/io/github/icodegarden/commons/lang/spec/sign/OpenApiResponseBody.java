package io.github.icodegarden.commons.lang.spec.sign;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class OpenApiResponseBody {
	private String code;
	private String msg;
	private String sub_code;
	private String sub_msg;
	private String biz_code;
	private String biz_content;
	private String sign;

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

	public String getBiz_code() {
		return biz_code;
	}

	public void setBiz_code(String biz_code) {
		this.biz_code = biz_code;
	}

	public String getBiz_content() {
		return biz_content;
	}

	public void setBiz_content(String biz_content) {
		this.biz_content = biz_content;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	@Override
	public String toString() {
		return "OpenApiResponseBody [code=" + code + ", msg=" + msg + ", sub_code=" + sub_code + ", sub_msg=" + sub_msg
				+ ", biz_code=" + biz_code + ", biz_content=" + biz_content + ", sign=" + sign + "]";
	}

}
