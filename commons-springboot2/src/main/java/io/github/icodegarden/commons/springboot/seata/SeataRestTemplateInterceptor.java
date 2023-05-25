package io.github.icodegarden.commons.springboot.seata;

import java.io.IOException;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;

import io.seata.core.context.RootContext;

public class SeataRestTemplateInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
			ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
		HttpRequestWrapper requestWrapper = new HttpRequestWrapper(httpRequest);

		String xid = RootContext.getXID();

		if (!StringUtils.isEmpty(xid)) {
			requestWrapper.getHeaders().add(RootContext.KEY_XID, xid);
		}
		return clientHttpRequestExecution.execute(requestWrapper, bytes);
	}

}
