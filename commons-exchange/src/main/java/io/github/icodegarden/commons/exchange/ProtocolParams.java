package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ProtocolParams {

	@Nullable
	private final String scheme;
	private final String ip;
	private final int port;
	private final Object body;
	private final int timeout;

	public ProtocolParams(@Nullable String scheme, String ip, int port, Object body, int timeout) {
		this.scheme = scheme;
		this.ip = ip;
		this.port = port;
		this.body = body;
		this.timeout = timeout;
	}

	public String getScheme() {
		return scheme;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public Object getBody() {
		return body;
	}

	public int getTimeout() {
		return timeout;
	}

	@Override
	public String toString() {
		return "ProtocolParams [scheme=" + scheme + ", ip=" + ip + ", port=" + port + ", body=" + body + ", timeout="
				+ timeout + "]";
	}

}