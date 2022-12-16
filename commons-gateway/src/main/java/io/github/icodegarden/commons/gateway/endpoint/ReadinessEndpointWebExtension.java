package io.github.icodegarden.commons.gateway.endpoint;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EndpointWebExtension(endpoint = ReadinessEndPoint.class)
public class ReadinessEndpointWebExtension {

	private final ReadinessEndPoint readinessEndPoint;

	public ReadinessEndpointWebExtension(ReadinessEndPoint readinessEndPoint) {
		this.readinessEndPoint = readinessEndPoint;
	}

	@ReadOperation
	public WebEndpointResponse<String> readiness() {
		String msg;
		int statusCode = 200;
		try {
			msg = readinessEndPoint.readiness();
		} catch (IllegalStateException e) {
			msg = e.getMessage();
			statusCode = 503;
		}

		return new WebEndpointResponse<>(msg, statusCode);
	}

}