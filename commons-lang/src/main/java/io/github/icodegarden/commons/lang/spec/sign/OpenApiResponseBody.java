package io.github.icodegarden.commons.lang.spec.sign;

import io.github.icodegarden.commons.lang.spec.response.ApiResponse;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class OpenApiResponseBody extends ApiResponse {

	private String biz_code;
	private String biz_content;
	private String sign;

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
		return "OpenApiResponseBody [biz_code=" + biz_code + ", biz_content=" + biz_content + ", sign=" + sign
				+ ", toString()=" + super.toString() + "]";
	}

}
