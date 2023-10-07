package io.github.icodegarden.commons.exchange.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ServerErrorRemoteException;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RestRequestTemplate {

	private static final String ACCEPT = "application/json";
	private static final String CONTENT_TYPE = "application/json";

	private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

	private int connectTimeout = -1;
	private int readTimeout = -1;
	
	public void setErrorHandler(ResponseErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType) throws RemoteException {
		URI uri = buildURI(url);

		RequestCallback requestCallback = new HttpEntityRequestCallback(request, null, responseType);
		HttpMessageConverterExtractor<T> extractor = new HttpMessageConverterExtractor<T>(responseType);
		return doExecute(uri, HttpMethod.POST, requestCallback, extractor);
	}

	public <T> HttpEntity<T> postForEntity(String url, @Nullable Object request, Class<T> responseType)
			throws RemoteException {
		URI uri = buildURI(url);

		RequestCallback requestCallback = new HttpEntityRequestCallback(request, null, responseType);
		ResponseEntityResponseExtractor<T> extractor = new ResponseEntityResponseExtractor<T>(responseType);
		return doExecute(uri, HttpMethod.POST, requestCallback, extractor);
	}

	/**
	 * 
	 * @param <T>
	 * @param url
	 * @param httpMethod
	 * @param httpEntity
	 * @param responseType 适用于直接的类型
	 * @return
	 * @throws RemoteException
	 */
	public <T> HttpEntity<T> exchange(String url, HttpMethod httpMethod, @Nullable HttpEntity<?> httpEntity,
			Class<T> responseType) throws RemoteException {
		Object request = null;
		HttpHeaders httpHeaders = null;
		if(httpEntity != null) {
			request = httpEntity.getBody();
			httpHeaders = httpEntity.getHeaders();
		}

		URI uri = buildURI(url);

		RequestCallback requestCallback = new HttpEntityRequestCallback(request, httpHeaders, responseType);
		ResponseEntityResponseExtractor<T> extractor = new ResponseEntityResponseExtractor<T>(responseType);
		return doExecute(uri, httpMethod, requestCallback, extractor);
	}

	/**
	 * 
	 * @param <T>
	 * @param url
	 * @param httpMethod
	 * @param httpEntity
	 * @param typeReference 适用于直接或间接类型，如容器再包装类型List<XXX>
	 * @return
	 * @throws RemoteException
	 */
	public <T> HttpEntity<T> exchange(String url, HttpMethod httpMethod, @Nullable HttpEntity<?> httpEntity,
			ParameterizedTypeReference<T> typeReference) throws RemoteException {
		Object request = null;
		HttpHeaders httpHeaders = null;
		if(httpEntity != null) {
			request = httpEntity.getBody();
			httpHeaders = httpEntity.getHeaders();
		}

		URI uri = buildURI(url);

		Type responseType = typeReference.getType();

		RequestCallback requestCallback = new HttpEntityRequestCallback(request, httpHeaders, responseType);
		ResponseEntityResponseExtractor<T> extractor = new ResponseEntityResponseExtractor<T>(responseType);
		return doExecute(uri, httpMethod, requestCallback, extractor);
	}

	private URI buildURI(String url) {
		try {
			return new URI(url);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private <T> T doExecute(URI url, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
			@Nullable ResponseExtractor<T> responseExtractor) throws RemoteException {
		Assert.notNull(url, "URI is required");
		Assert.notNull(method, "HttpMethod is required");
		ClientHttpResponse response = null;
		try {
			ClientHttpRequest request = createRequest(url, method);
			if (requestCallback != null) {
				requestCallback.doWithRequest(request);
			}
			response = request.execute();
			handleResponse(url, method, response);
			return responseExtractor != null ? responseExtractor.extractData(response) : null;
		} catch (IOException ex) {
			String resource = url.toString();
			String query = url.getRawQuery();
			resource = (query != null ? resource.substring(0, resource.indexOf('?')) : resource);

//			String msgSuffix = "";
//			if (ex instanceof HttpRetryException) {
//				int httpStatus = ((HttpRetryException) ex).responseCode();
//				msgSuffix = "httpStatus:" + httpStatus;
//			}

			throw new ExceedExpectedRemoteException(
					"I/O error on " + method.name() + " request for \"" + resource + "\": " + ex.getMessage(), ex);
		} finally {
			if (response != null) {
				response.close();
			}
		}
	}

	private ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
		HttpURLConnection connection = openConnection(uri.toURL());
		prepareConnection(connection, httpMethod.name());

		return new ClientHttpRequest(connection, true);
	}

	private HttpURLConnection openConnection(URL url) throws IOException {
		URLConnection urlConnection = url.openConnection();
		if (!(urlConnection instanceof HttpURLConnection)) {
			throw new IllegalStateException("HttpURLConnection required for [" + url + "] but got: " + urlConnection);
		}
		return (HttpURLConnection) urlConnection;
	}

	private void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
		if (this.connectTimeout >= 0) {
			connection.setConnectTimeout(this.connectTimeout);
		}
		if (this.readTimeout >= 0) {
			connection.setReadTimeout(this.readTimeout);
		}

		boolean mayWrite = ("POST".equals(httpMethod) || "PUT".equals(httpMethod) || "PATCH".equals(httpMethod)
				|| "DELETE".equals(httpMethod));

		connection.setDoInput(true);
		connection.setInstanceFollowRedirects("GET".equals(httpMethod));
		connection.setDoOutput(mayWrite);
		connection.setRequestMethod(httpMethod);
	}

	private void handleResponse(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		ResponseErrorHandler errorHandler = this.errorHandler;
		boolean hasError = errorHandler.hasError(response);

		if (hasError) {
			errorHandler.handleError(url, method, response);
		}
	}

	private interface RequestCallback {
		void doWithRequest(ClientHttpRequest request) throws IOException;
	}

	private class AcceptHeaderRequestCallback implements RequestCallback {

		@Nullable
		private final Type responseType;

		public AcceptHeaderRequestCallback(@Nullable Type responseType) {
			this.responseType = responseType;
		}

		@Override
		public void doWithRequest(ClientHttpRequest request) throws IOException {
			request.getHeaders().set("Accept", ACCEPT);
		}
	}

	private class HttpEntityRequestCallback extends AcceptHeaderRequestCallback {
		private Object requestBody;
		private HttpHeaders httpHeaders;

		public HttpEntityRequestCallback(@Nullable Object requestBody, @Nullable HttpHeaders httpHeaders,
				@Nullable Type responseType) {
			super(responseType);
			this.requestBody = requestBody;
			this.httpHeaders = httpHeaders == null ? new HttpHeaders() : httpHeaders;
		}

		@Override
		public void doWithRequest(ClientHttpRequest httpRequest) throws IOException {
			super.doWithRequest(httpRequest);
			Object requestBody = this.requestBody;
			if (requestBody == null) {
				HttpHeaders httpHeaders = httpRequest.getHeaders();
				HttpHeaders requestHeaders = this.httpHeaders;
				if (!requestHeaders.isEmpty()) {
					requestHeaders.forEach((key, values) -> httpHeaders.put(key, new ArrayList<>(values)));
				}
				if (httpHeaders.getContentLength() < 0) {
					httpHeaders.setContentLength(0L);
				}
			} else {
				HttpHeaders httpHeaders = httpRequest.getHeaders();
				HttpHeaders requestHeaders = this.httpHeaders;
				if (!requestHeaders.isEmpty()) {
					requestHeaders.forEach((key, values) -> httpHeaders.put(key, new ArrayList<>(values)));
				}

				String json = JsonUtils.serialize(requestBody);
				byte[] bytes = json.getBytes();
				httpRequest.getBufferedOutput().write(bytes);

				if (httpHeaders.getContentLength() < 0) {
					httpHeaders.setContentLength(bytes.length);
				}
			}

			httpRequest.getHeaders().set("Content-Type", CONTENT_TYPE);
		}
	}

	private interface ResponseExtractor<T> {
		@Nullable
		T extractData(ClientHttpResponse response) throws IOException;
	}

	private class HttpMessageConverterExtractor<T> implements ResponseExtractor<T> {
		private final Type responseType;
		private final Class<T> responseClass;

		HttpMessageConverterExtractor(Type responseType) {
			Assert.notNull(responseType, "responseType must not be null");
			this.responseType = responseType;
			this.responseClass = (responseType instanceof Class ? (Class<T>) responseType : null);
		}

		@Override
		public T extractData(ClientHttpResponse response) throws IOException {
			if (response.getRawStatusCode() >= 400) {
				throw new IOException("request result:" + response.getRawStatusCode());
			}
			if (responseType == null) {
				return null;
			}
			if (responseType == String.class) {
				int available = response.getBody().available();
				byte[] bs = new byte[available];
				response.getBody().read(bs);
				return (T) new String(bs, "utf-8");
			} else if(responseType == Void.class) {
				return null;
			}

			return JsonUtils.deserialize(response.getBody(), responseType);
		}
	}

	private class ResponseEntityResponseExtractor<T> implements ResponseExtractor<HttpEntity<T>> {
		private final HttpMessageConverterExtractor<T> delegator;

		ResponseEntityResponseExtractor(Type responseType) {
			this.delegator = new HttpMessageConverterExtractor<T>(responseType);
		}

		@Override
		public HttpEntity<T> extractData(ClientHttpResponse response) throws IOException {
			T t = delegator.extractData(response);
			return new HttpEntity<T>(t, response.getHeaders());
		}
	}

	public static class DefaultResponseErrorHandler implements ResponseErrorHandler {
		
		private final Collection<String> extractHeadersOn4xx;
		private final Collection<String> extractHeadersOn5xx;

		public DefaultResponseErrorHandler() {
			this(null, null);
		}
		
		public DefaultResponseErrorHandler(Collection<String> extractHeadersOn4xx,Collection<String> extractHeadersOn5xx) {
			this.extractHeadersOn4xx = extractHeadersOn4xx;
			this.extractHeadersOn5xx = extractHeadersOn5xx;
		}

		public boolean hasError(ClientHttpResponse response) throws IOException {
			int rawStatusCode = response.getRawStatusCode();
			return rawStatusCode >= 400;
		}

		public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
			handleError(response);
		}

		private void handleError(ClientHttpResponse response) throws IOException {
			int statusCode = response.getRawStatusCode();
//			if (statusCode == null) {
//				byte[] body = getResponseBody(response);
//				String message = getErrorMessage(response.getRawStatusCode(),
//						response.getStatusText(), body, getCharset(response));
//				throw new UnknownHttpStatusCodeException(message,
//						response.getRawStatusCode(), response.getStatusText(),
//						response.getHeaders(), body, getCharset(response));
//			}
			handleError(response, statusCode);
		}

		/**
		 * Return error message with details from the response body, possibly truncated:
		 * 
		 * <pre>
		 * 404 Not Found: [{'id': 123, 'message': 'my very long... (500 bytes)]
		 * </pre>
		 */
		private String getErrorMessage(int rawStatusCode, String statusText, @Nullable byte[] responseBody,
				@Nullable Charset charset) {

			String preface = rawStatusCode + " " + statusText + ": ";
			if (responseBody == null || responseBody.length == 0) {
				return preface + "[no body]";
			}

			if (charset == null) {
				charset = StandardCharsets.UTF_8;
			}
			int maxChars = 200;

			if (responseBody.length < maxChars * 2) {
				return preface + "[" + new String(responseBody, charset) + "]";
			}

			try {
				Reader reader = new InputStreamReader(new ByteArrayInputStream(responseBody), charset);
				CharBuffer buffer = CharBuffer.allocate(maxChars);
				reader.read(buffer);
				reader.close();
				buffer.flip();
				return preface + "[" + buffer.toString() + "... (" + responseBody.length + " bytes)]";
			} catch (IOException ex) {
				// should never happen
				throw new IllegalStateException(ex);
			}
		}

		private void handleError(ClientHttpResponse response, int statusCode) throws IOException {
			String statusText = response.getStatusText();
			HttpHeaders httpHeaders = response.getHeaders();
			byte[] body = getResponseBody(response);
			Charset charset = getCharset(response);
			String message = getErrorMessage(statusCode, statusText, body, charset);

			switch (Integer.toString(statusCode).charAt(0)) {
			case '4':
				if(extractHeadersOn4xx != null) {
					List<String> removeNames = httpHeaders.keySet().stream()
							.filter(key -> !extractHeadersOn4xx.contains(key)).collect(Collectors.toList());
					removeNames.forEach(removeName -> {
						httpHeaders.remove(removeName);
					});
				}
				throw new HttpClientRemoteException(message, statusCode, httpHeaders);
//					throw HttpClientErrorException.create(message, statusCode, statusText, headers, body, charset);
			case '5':
				if(extractHeadersOn5xx != null) {
					List<String> removeNames = httpHeaders.keySet().stream()
							.filter(key -> !extractHeadersOn5xx.contains(key)).collect(Collectors.toList());
					removeNames.forEach(removeName -> {
						httpHeaders.remove(removeName);
					});
				}
				throw new ServerErrorRemoteException(message);
//					throw HttpServerErrorException.create(message, statusCode, statusText, headers, body, charset);
			default:
				throw new ExceedExpectedRemoteException(message);
//					throw new UnknownHttpStatusCodeException(message, statusCode.value(), statusText, headers, body, charset);
			}
		}

		/**
		 * Read the body of the given response (for inclusion in a status exception).
		 * 
		 * @param response the response to inspect
		 * @return the response body as a byte array, or an empty byte array if the body
		 *         could not be read
		 * @since 4.3.8
		 */
		private byte[] getResponseBody(ClientHttpResponse response) {
			try {
//				return FileCopyUtils.copyToByteArray(response.getBody());
				int available = response.getBody().available();
				byte[] bs = new byte[available];
				response.getBody().read(bs);
				return bs;
			} catch (IOException ex) {
				// ignore
			}
			return new byte[0];
		}

		/**
		 * Determine the charset of the response (for inclusion in a status exception).
		 * 
		 * @param response the response to inspect
		 * @return the associated charset, or {@code null} if none
		 * @since 4.3.8
		 */
		@Nullable
		private Charset getCharset(ClientHttpResponse response) {
//			HttpHeaders headers = response.getHeaders();
//			MediaType contentType = headers.getContentType();
//			return (contentType != null ? contentType.getCharset() : null);
			return Charset.forName("utf-8");
		}

	}

}