package io.github.icodegarden.commons.redis.args;

import java.util.Map;
import java.util.Objects;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public class StreamMessage<K, V> {

	private final K stream;

	private final String id;

	private final Map<K, V> body;

	/**
	 * Create a new {@link StreamMessage}.
	 *
	 * @param stream the stream.
	 * @param id     the message id.
	 * @param body   map containing the message body.
	 */
	public StreamMessage(K stream, String id, Map<K, V> body) {

		this.stream = stream;
		this.id = id;
		this.body = body;
	}

	public K getStream() {
		return stream;
	}

	public String getId() {
		return id;
	}

	/**
	 * @return the message body. Can be {@code null} for commands that do not return
	 *         the message body.
	 */
	public Map<K, V> getBody() {
		return body;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof StreamMessage))
			return false;
		StreamMessage<?, ?> that = (StreamMessage<?, ?>) o;
		return Objects.equals(stream, that.stream) && Objects.equals(id, that.id) && Objects.equals(body, that.body);
	}

	@Override
	public int hashCode() {
		return Objects.hash(stream, id, body);
	}

	@Override
	public String toString() {
		return String.format("StreamMessage[%s:%s]%s", stream, id, body);
	}

}