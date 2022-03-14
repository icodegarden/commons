package io.github.icodegarden.commons.exchange.http;

import java.lang.reflect.Type;
import java.util.Collection;

import org.springframework.core.ParameterizedTypeReference;

import io.github.icodegarden.commons.exchange.Protocol;
import io.github.icodegarden.commons.exchange.ProtocolParams;
import io.github.icodegarden.commons.exchange.http.RestRequestTemplate.DefaultResponseErrorHandler;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class SimpleRestHttpProtocol implements Protocol {
//	private static final Logger log = LoggerFactory.getLogger(NioProtocol.class);

	private String path;
	private HttpMethod httpMethod;
	private HttpHeaders httpHeaders;
	private int connectTimeout = -1;
	private int readTimeout = -1;
	private Type responseType;
	private Collection<String> extractHeadersOn4xx;
	private Collection<String> extractHeadersOn5xx;

	private HttpEntity<?> responseEntity;

	public SimpleRestHttpProtocol(String path, HttpMethod httpMethod, Class<?> responseType) {
		this(path, httpMethod, null, responseType);
	}

	public SimpleRestHttpProtocol(String path, HttpMethod httpMethod, HttpHeaders httpHeaders, Class<?> responseType) {
		this.path = path;
		this.httpMethod = httpMethod;
		this.httpHeaders = httpHeaders;
		this.responseType = responseType;
	}

	public SimpleRestHttpProtocol(String path, HttpMethod httpMethod, ParameterizedTypeReference<?> typeReference) {
		this(path, httpMethod, null, typeReference);
	}

	public SimpleRestHttpProtocol(String path, HttpMethod httpMethod, HttpHeaders httpHeaders,
			ParameterizedTypeReference<?> typeReference) {
		this.path = path;
		this.httpMethod = httpMethod;
		this.httpHeaders = httpHeaders;
		this.responseType = typeReference.getType();
	}

	@SuppressWarnings("all")
	@Override
	public HttpEntity exchange(ProtocolParams params) throws RemoteException {
		RestRequestTemplate restRequestTemplate = new RestRequestTemplate();
		restRequestTemplate.setConnectTimeout(connectTimeout);
		restRequestTemplate.setReadTimeout(readTimeout);
		DefaultResponseErrorHandler errorHandler = new RestRequestTemplate.DefaultResponseErrorHandler(
				extractHeadersOn4xx, extractHeadersOn5xx);
		restRequestTemplate.setErrorHandler(errorHandler);

		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		StringBuilder sb = new StringBuilder(128);
		if (params.getScheme() != null) {
			sb.append(params.getScheme()).append("://");
		} else {
			sb.append("http://");
		}
		sb.append(params.getIp());
		sb.append(":");
		sb.append(params.getPort());
		sb.append("/");
		sb.append(path);

		String url = sb.toString();
		HttpEntity<Object> httpEntity = new HttpEntity<>(params.getBody(), httpHeaders);

		ParameterizedTypeReference typeReference = ParameterizedTypeReference.forType(responseType);
		responseEntity = restRequestTemplate.exchange(url, httpMethod, httpEntity, typeReference);
		return responseEntity;
	}

	@SuppressWarnings("all")
	public HttpEntity getResponseEntity() {
		return responseEntity;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setHttpHeaders(HttpHeaders httpHeaders) {
		this.httpHeaders = httpHeaders;
	}

	public void setExtractHeadersOn4xx(Collection<String> extractHeadersOn4xx) {
		this.extractHeadersOn4xx = extractHeadersOn4xx;
	}

	public void setExtractHeadersOn5xx(Collection<String> extractHeadersOn5xx) {
		this.extractHeadersOn5xx = extractHeadersOn5xx;
	}

}