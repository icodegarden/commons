package io.github.icodegarden.commons.exchange.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ClientHttpResponse {
	private static final int DRAIN_BUFFER_SIZE = 4096;

	private final HttpURLConnection connection;

	@Nullable
	private HttpHeaders headers;

	@Nullable
	private InputStream responseStream;

	ClientHttpResponse(HttpURLConnection connection) {
		this.connection = connection;
	}

//	public HttpStatus getStatusCode() throws IOException {
//		return HttpStatus.valueOf(getRawStatusCode());
//	}

	int getRawStatusCode() throws IOException {
		return this.connection.getResponseCode();
	}

	String getStatusText() throws IOException {
		String result = this.connection.getResponseMessage();
		return (result != null) ? result : "";
	}

	HttpHeaders getHeaders() {
		if (this.headers == null) {
			this.headers = new HttpHeaders();
			// Header field 0 is the status line for most HttpURLConnections, but not on GAE
			String name = this.connection.getHeaderFieldKey(0);
			if (name != null && !name.isEmpty()) {
				this.headers.add(name, this.connection.getHeaderField(0));
			}
			int i = 1;
			while (true) {
				name = this.connection.getHeaderFieldKey(i);
				if (name == null || name.isEmpty()) {
					break;
				}
				this.headers.add(name, this.connection.getHeaderField(i));
				i++;
			}
		}
		return this.headers;
	}

	InputStream getBody() throws IOException {
		InputStream errorStream = this.connection.getErrorStream();
		this.responseStream = (errorStream != null ? errorStream : this.connection.getInputStream());
		return this.responseStream;
	}

	void close() {
		try {
			if (this.responseStream == null) {
				getBody();
			}
			drain(this.responseStream);
			this.responseStream.close();
		} catch (Exception ex) {
			// ignore
		}
	}

	private int drain(InputStream in) throws IOException {
		Assert.notNull(in, "No InputStream specified");
		byte[] buffer = new byte[DRAIN_BUFFER_SIZE];
		int bytesRead = -1;
		int byteCount = 0;
		while ((bytesRead = in.read(buffer)) != -1) {
			byteCount += bytesRead;
		}
		return byteCount;
	}
}