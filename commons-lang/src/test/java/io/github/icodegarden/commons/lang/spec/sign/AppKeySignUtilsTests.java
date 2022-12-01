package io.github.icodegarden.commons.lang.spec.sign;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class AppKeySignUtilsTests {

	private static final String appKey = "edcrfv567tgbnjkdfgcghjoqaz";

	OpenApiRequestBody openApiBody = new OpenApiRequestBody();
	{
		openApiBody.setApp_id("2014072300007148");
		openApiBody.setMethod("type.software.part");
		openApiBody.setFormat("JSON");
		openApiBody.setCharset("utf-8");
		openApiBody.setSign_type("SHA256");
		openApiBody.setTimestamp("2014-07-24 03:07:50");
		openApiBody.setVersion("1.0");
		openApiBody.setRequest_id("1624613288981");
		openApiBody.setBiz_content("{\"partNumber\":\"8888111156\"}");
	}

	@Test
	void requestSign() {
		System.out.println("test requestSign origin body:"+JsonUtils.serialize(openApiBody));
		
		String sign = AppKeySignUtils.requestSign(openApiBody, appKey);
		System.out.println(sign);
		openApiBody.setSign(sign);

		boolean validateSign = AppKeySignUtils.validateRequestSign(openApiBody, appKey);
		assertThat(validateSign).isTrue();
	}

	@Test
	void responseSign() {
		OpenApiResponse openApiResponse = OpenApiResponse.success(openApiBody.getMethod(), "{\"partType\":\"A\"}");
		
		String sign = AppKeySignUtils.responseSign(openApiResponse, openApiBody.getSign_type(), appKey);
		System.out.println(sign);
		openApiResponse.setSign(sign);

		boolean validateResponseSign = AppKeySignUtils.validateResponseSign(openApiResponse,
				openApiBody.getSign_type(), appKey);
		assertThat(validateResponseSign).isTrue();
	}
}
