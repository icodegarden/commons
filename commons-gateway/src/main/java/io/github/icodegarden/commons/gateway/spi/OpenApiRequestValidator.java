package io.github.icodegarden.commons.gateway.spi;

import io.github.icodegarden.commons.gateway.core.security.signature.App;
import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;

/**
 * 
 * 
 * @author Fangfang.Xu
 *
 */
public interface OpenApiRequestValidator {

	/**
	 * @return 是否允许请求
	 */
	void validate(String requestPath, OpenApiRequestBody requestBody, App app);
}
