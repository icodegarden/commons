package io.github.icodegarden.commons.exchange.http;

import java.io.IOException;
import java.net.URI;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ResponseErrorHandler {

	boolean hasError(ClientHttpResponse response) throws IOException;

	void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException;
}