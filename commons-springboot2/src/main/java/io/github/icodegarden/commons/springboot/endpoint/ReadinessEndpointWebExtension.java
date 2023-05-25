package io.github.icodegarden.commons.springboot.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EndpointWebExtension(endpoint = ReadinessEndpoint.class)
public class ReadinessEndpointWebExtension {

	private final ReadinessEndpoint readinessEndpoint;

	public ReadinessEndpointWebExtension(ReadinessEndpoint readinessEndpoint) {
		this.readinessEndpoint = readinessEndpoint;
	}

	@ReadOperation
	public WebEndpointResponse<String> readiness() {
		String msg;
		int statusCode = 200;
		try {
			msg = readinessEndpoint.readiness().name();
		} catch (IllegalStateException e) {
			msg = e.getMessage();
			statusCode = 503;
		}

		return new WebEndpointResponse<>(msg, statusCode);
	}

}