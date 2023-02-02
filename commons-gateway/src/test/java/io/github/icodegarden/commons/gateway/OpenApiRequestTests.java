package io.github.icodegarden.commons.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class OpenApiRequestTests {

	/**
	 * 模拟访问一个后端openapi
	 */
	@Test
	void request() throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		OpenApiRequestBody body = JsonUtils.deserialize(
				"{\r\n" + "    \"app_id\": \"dev_ota_cn_appid\",\r\n"
						+ "    \"method\": \"consumer.metadata.system\",\r\n" + "    \"format\": \"JSON\",\r\n"
						+ "    \"charset\": \"utf-8\",\r\n" + "    \"sign_type\": \"SHA256\",\r\n"
						+ "    \"timestamp\": \"2014-07-24 03:07:50\",\r\n" + "    \"version\": \"1.0\",\r\n"
						+ "    \"request_id\": \"1624613288981\",\r\n"
						+ "    \"biz_content\": \"{\\\"partNumber\\\":\\\"8888111156\\\"}\",\r\n"
						+ "    \"sign\":\"54B1A9C38E86B58947DE201C74FB83994493D2DFEBB17F6A694FA01446119047\"\r\n" + "}",
				OpenApiRequestBody.class);

		int i = 0;
		for (;;) {
			try {
				String postForObject = restTemplate.postForObject("http://localhost:8080/openapi/v1/biz/methods", body,
						String.class);
				System.out.println(postForObject);
			} catch (Exception e) {
				System.out.println(e);
			}

			System.out.println(i++);
		}
	}
}
