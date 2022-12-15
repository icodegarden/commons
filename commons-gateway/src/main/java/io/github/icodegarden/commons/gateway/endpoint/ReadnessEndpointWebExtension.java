package io.github.icodegarden.commons.gateway.endpoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@EndpointWebExtension(endpoint = ReadnessEndPoint.class)
public class ReadnessEndpointWebExtension {

	@Autowired
	private ReadnessEndPoint readnessEndPoint;

	@ReadOperation
	public WebEndpointResponse<String> readness() {
		String msg;
		int statusCode = 200;
		try {
			msg = readnessEndPoint.readness();
		} catch (IllegalStateException e) {
			msg = e.getMessage();
			statusCode = 503;
		}

		return new WebEndpointResponse<>(msg, statusCode);
	}

}