package io.github.icodegarden.commons.gateway.spi;

import io.github.icodegarden.commons.lang.spec.sign.OpenApiRequestBody;

/**
 * 可用于防重放等
 * 
 * @author Fangfang.Xu
 *
 */
public interface OpenApiRequestValidator {

	/**
	 * @return 是否允许请求
	 */
	boolean validate(OpenApiRequestBody requestBody);
}
