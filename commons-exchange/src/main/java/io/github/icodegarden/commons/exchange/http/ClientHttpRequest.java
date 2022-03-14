package io.github.icodegarden.commons.exchange.http;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ClientHttpRequest {
	private boolean executed = false;

	private final HttpURLConnection connection;
	private final boolean outputStreaming;

	private HttpHeaders headers = new HttpHeaders();

	private ByteArrayOutputStream bufferedOutput = new ByteArrayOutputStream(1024);

	ClientHttpRequest(HttpURLConnection connection, boolean outputStreaming) {
		this.connection = connection;
		this.outputStreaming = outputStreaming;
	}

	HttpHeaders getHeaders() {
		return headers;
	}

	ClientHttpResponse execute() throws IOException {
		assertNotExecuted();
		ClientHttpResponse result = executeInternal(this.headers);
		this.executed = true;
		return result;
	}

	private void assertNotExecuted() {
		Assert.state(!this.executed, "ClientHttpRequest already executed");
	}

	ByteArrayOutputStream getBufferedOutput() {
		return bufferedOutput;
	}

	private ClientHttpResponse executeInternal(HttpHeaders headers) throws IOException {
		byte[] bytes = this.bufferedOutput.toByteArray();
		if (headers.getContentLength() < 0) {
			headers.setContentLength(bytes.length);
		}
		ClientHttpResponse result = executeInternal(headers, bytes);
		this.bufferedOutput = new ByteArrayOutputStream(0);
		return result;
	}

	private ClientHttpResponse executeInternal(HttpHeaders headers, byte[] bufferedOutput) throws IOException {
		addHeaders(this.connection, headers);
		// JDK <1.8 doesn't support getOutputStream with HTTP DELETE
//			if (getMethod() == HttpMethod.DELETE && bufferedOutput.length == 0) {
		if ("DELETE".equalsIgnoreCase(connection.getRequestMethod()) && bufferedOutput.length == 0) {
			this.connection.setDoOutput(false);
		}
		if (this.connection.getDoOutput() && this.outputStreaming) {
			this.connection.setFixedLengthStreamingMode(bufferedOutput.length);
		}
		this.connection.connect();
		if (this.connection.getDoOutput()) {
			copy(bufferedOutput, this.connection.getOutputStream());
		} else {
			// Immediately trigger the request in a no-output scenario as well
			this.connection.getResponseCode();
		}
		return new ClientHttpResponse(this.connection);
	}

	private void copy(byte[] in, OutputStream out) throws IOException {
		Assert.notNull(in, "No input byte array specified");
		Assert.notNull(out, "No OutputStream specified");

		try {
			out.write(in);
		} finally {
			close(out);
		}
	}

	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException ex) {
			// ignore
		}
	}

	private void addHeaders(HttpURLConnection connection, HttpHeaders headers) {
		String method = connection.getRequestMethod();
		if (method.equals("PUT") || method.equals("DELETE")) {
			String accept = headers.getFirst("Accept");
			if (accept == null || accept.isEmpty()) {
				// Avoid "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"
				// from HttpUrlConnection which prevents JSON error response details.
				headers.set("Accept", "*/*");
			}
		}
		headers.forEach((headerName, headerValues) -> {
			if ("Cookie".equalsIgnoreCase(headerName)) { // RFC 6265
//				String headerValue = StringUtils.collectionToDelimitedString(headerValues, "; ");
				String headerValue = headerValues.stream().collect(Collectors.joining("; "));
				connection.setRequestProperty(headerName, headerValue);
			} else {
				for (String headerValue : headerValues) {
					String actualHeaderValue = headerValue != null ? headerValue : "";
					connection.addRequestProperty(headerName, actualHeaderValue);
				}
			}
		});
	}
}