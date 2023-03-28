package io.github.icodegarden.commons.lang.spec.sign;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 带签名的请求体生成工具，便于测试用
 * 
 * @author Fangfang.Xu
 *
 */
class OpenapiUtils {

	public static void main(String[] args) {
		String app_id = "dev_sample_appid";
		String app_key = "dev_sample_appkey";
		String biz_code = "consumer.metadata.system";
		String biz_content = "{\"name\":\"中国\"}";

		baseOpenApiRequestBody(app_id, biz_code, biz_content, app_key);
	}

	private static OpenApiRequestBody baseOpenApiRequestBody(String appId, String method, String bizContent,
			String appKey) {
		OpenApiRequestBody body = new OpenApiRequestBody();
		body.setApp_id(appId);
		body.setCharset("utf-8");
		body.setFormat("JSON");
		body.setMethod(method);
		body.setRequest_id(System.currentTimeMillis() + "");
		body.setSign_type("SHA256");
		body.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));
		body.setVersion("1.0");
		body.setBiz_content(bizContent);

		String sign = AppKeySignUtils.requestSign(body, appKey);
		body.setSign(sign);

		String json = JsonUtils.serialize(body);
		System.out.println(json);

		return body;
	}
}