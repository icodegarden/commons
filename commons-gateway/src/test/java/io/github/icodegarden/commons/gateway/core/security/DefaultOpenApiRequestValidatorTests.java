package io.github.icodegarden.commons.gateway.core.security;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

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

	@Test
	void validate_false_timestampBefore() throws Exception {
		DefaultOpenApiRequestValidator validator = new DefaultOpenApiRequestValidator();
		
		OpenApiRequestBody requestBody = new OpenApiRequestBody();
		requestBody.setTimestamp(SystemUtils.STANDARD_DATETIME_FORMATTER.format(LocalDateTime.now().minusMinutes(6)));
		
		boolean validate = validator.validate(requestBody);
		Assertions.assertThat(validate).isFalse();
	}
}
