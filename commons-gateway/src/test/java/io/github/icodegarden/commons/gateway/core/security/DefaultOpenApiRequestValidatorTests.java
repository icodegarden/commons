package io.github.icodegarden.commons.gateway.core.security;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class DefaultOpenApiRequestValidatorTests {

	/**
	 * 时间过早，false
	 */
	@Test
	void validate_timestampBefore() throws Exception {
		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();

		OpenApiRequestBody requestBody = new OpenApiRequestBody();
		requestBody.setApp_id("app_id");
		requestBody.setRequest_id("request_id");
		requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now().minusMinutes(6)));

		boolean validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isFalse();
	}

	/**
	 * 不同2次请求，1true 2true
	 */
	@Test
	void validate_notDuplicateRequest() throws Exception {
		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();

		OpenApiRequestBody requestBody = new OpenApiRequestBody();
		requestBody.setApp_id("app_id");
		requestBody.setRequest_id("request_id");
		requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now()));

		boolean validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isTrue();
		requestBody.setRequest_id("request_id2");
		validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isTrue();// 第二次使用不同request_id
	}

	/**
	 * 重复2次请求，1true 2false
	 */
	@Test
	void validate_duplicateRequest() throws Exception {
		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();

		OpenApiRequestBody requestBody = new OpenApiRequestBody();
		requestBody.setApp_id("app_id");
		requestBody.setRequest_id("request_id");
		requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now()));

		boolean validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isTrue();
		validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isFalse();// 第二次重复
	}

	/**
	 * 重复2次请求但不同的app，1true 2true
	 */
	@Test
	void validate_DuplicateRequest_2app() throws Exception {
		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();

		OpenApiRequestBody requestBody = new OpenApiRequestBody();
		requestBody.setApp_id("app_id");
		requestBody.setRequest_id("request_id");
		requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now()));

		boolean validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isTrue();

		requestBody.setApp_id("app_id2");
		validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isTrue();// 不同app
	}

	/**
	 * 限制最大堆内存 -Xmx10m(5m不能启动成功)<br>
	 * 2个app各15W次请求
	 */
	@Test
	void validate_loop() throws Exception {
		/**
		 * 可用于验证确实设置了内存限制
		 */
//		Assertions.assertThatExceptionOfType(OutOfMemoryError.class).isThrownBy(() -> {
//			byte[] bs = new byte[1024 * 1024 * 11];
//		});

		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();

		int max = 2;
		CountDownLatch countDownLatch = new CountDownLatch(max);
		for (int n = 0; n < max; n++) {
			final int num = n;
			new Thread() {
				public void run() {
					OpenApiRequestBody requestBody = new OpenApiRequestBody();
					requestBody.setApp_id("app_id" + num);
					/**
					 * 无论多少次都不会导致OOM的，这里为了快速结束数量少
					 */
					for (int i = 0; i < 10000; i++) {
						if (i % 1000 == 0) {
							System.out.println("loop, num=" + num + ", i=" + i);
						}

						requestBody.setRequest_id(UUID.randomUUID().toString() + UUID.randomUUID().toString()
								+ UUID.randomUUID().toString() + UUID.randomUUID().toString()
								+ UUID.randomUUID().toString() + UUID.randomUUID().toString()
								+ UUID.randomUUID().toString() + UUID.randomUUID().toString()
								+ UUID.randomUUID().toString() + UUID.randomUUID().toString());
						requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now()));

						boolean validate = validator.validate(requestBody);
						if (!validate) {
							System.err.println("validate is false, num=" + num + ", i=" + i);
							System.exit(-1);
						}
					}

					int size = validator.getAppExistRequestIdSize(requestBody.getApp_id());
					System.out.println("getAppExistRequestIdSize, num=" + num + ", size=" + size);

					countDownLatch.countDown();
				};
			}.start();
		}

		countDownLatch.await();
	}

}
