package io.github.icodegarden.commons.lang.spec.sign;

import io.github.icodegarden.commons.lang.annotation.NotEmpty;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class OpenApiRequestBody {

	@NotEmpty
	private String app_id;// 2014072300007148
	@NotEmpty
	private String method;// 接口名称
	@NotEmpty
	private String format;// json
	@NotEmpty
	private String charset;// utf-8
	@NotEmpty
	private String sign_type;// RSA2
	@NotEmpty
	private String sign;
	@NotEmpty
	private String timestamp;// 格式"yyyy-MM-dd HH:mm:ss" 2014-07-24 03:07:50
	@NotEmpty
	private String version;// 固定为：1.0
	@NotEmpty
	private String request_id;// 防重放
	@NotEmpty
	private String biz_content;// 请求参数的集合，最大长度不限，除公共参数外所有请求参数都必须放在这个参数中传递

	public String getApp_id() {
		return app_id;
	}

	public void setApp_id(String app_id) {
		this.app_id = app_id;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getSign_type() {
		return sign_type;
	}

	public void setSign_type(String sign_type) {
		this.sign_type = sign_type;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getRequest_id() {
		return request_id;
	}

	public void setRequest_id(String request_id) {
		this.request_id = request_id;
	}

	public String getBiz_content() {
		return biz_content;
	}

	public void setBiz_content(String biz_content) {
		this.biz_content = biz_content;
	}

	public String toStringExcludeBizContent() {
		return "OpenApiRequestBody [app_id=" + app_id + ", method=" + method + ", format=" + format + ", charset="
				+ charset + ", sign_type=" + sign_type + ", sign=" + sign + ", timestamp=" + timestamp + ", version="
				+ version + ", request_id=" + request_id + "]";
	}

	@Override
	public String toString() {
		return "OpenApiRequestBody [app_id=" + app_id + ", method=" + method + ", format=" + format + ", charset="
				+ charset + ", sign_type=" + sign_type + ", sign=" + sign + ", timestamp=" + timestamp + ", version="
				+ version + ", request_id=" + request_id + ", biz_content=" + biz_content + "]";
	}

}
