package io.github.icodegarden.commons.exchange.http;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
public class HttpEntity<T> {

	/**
	 * The empty {@code HttpEntity}, with no body or headers.
	 */
	public static final HttpEntity<?> EMPTY = new HttpEntity<>();

	private final HttpHeaders headers;

	@Nullable
	private final T body;

	/**
	 * Create a new, empty {@code HttpEntity}.
	 */
	protected HttpEntity() {
		this(null, null);
	}

	/**
	 * Create a new {@code HttpEntity} with the given body and no headers.
	 * 
	 * @param body the entity body
	 */
	public HttpEntity(T body) {
		this(body, null);
	}

	/**
	 * Create a new {@code HttpEntity} with the given headers and no body.
	 * 
	 * @param headers the entity headers
	 */
	public HttpEntity(HttpHeaders headers) {
		this(null, headers);
	}

	/**
	 * Create a new {@code HttpEntity} with the given body and headers.
	 * 
	 * @param body    the entity body
	 * @param headers the entity headers
	 */
	public HttpEntity(@Nullable T body, @Nullable HttpHeaders headers) {
		this.body = body;
		this.headers = headers != null ? headers : new HttpHeaders();
	}

	/**
	 * Returns the headers of this entity.
	 */
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Returns the body of this entity.
	 */
	@Nullable
	public T getBody() {
		return this.body;
	}

	/**
	 * Indicates whether this entity has a body.
	 */
	public boolean hasBody() {
		return (this.body != null);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("<");
		if (this.body != null) {
			builder.append(this.body);
			builder.append(',');
		}
		builder.append(this.headers);
		builder.append('>');
		return builder.toString();
	}

}