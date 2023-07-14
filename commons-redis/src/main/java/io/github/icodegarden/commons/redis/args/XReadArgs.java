package io.github.icodegarden.commons.redis.args;

import java.time.Duration;

import io.lettuce.core.internal.LettuceAssert;
import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class XReadArgs {

	private Long block;

	private Long count;

	private boolean noack;

	/**
	 * Perform a blocking read and wait up to {@code milliseconds} for a new stream
	 * message.
	 *
	 * @param milliseconds max time to wait.
	 * @return {@code this}.
	 */
	public XReadArgs block(long milliseconds) {

		this.block = milliseconds;
		return this;
	}

	/**
	 * Perform a blocking read and wait up to a {@link Duration timeout} for a new
	 * stream message.
	 *
	 * @param timeout max time to wait.
	 * @return {@code this}.
	 */
	public XReadArgs block(Duration timeout) {

		LettuceAssert.notNull(timeout, "Block timeout must not be null");

		return block(timeout.toMillis());
	}

	/**
	 * Limit read to {@code count} messages.
	 *
	 * @param count number of messages.
	 * @return {@code this}.
	 */
	public XReadArgs count(long count) {

		this.count = count;
		return this;
	}

	/**
	 * Use NOACK option to disable auto-acknowledgement. Only valid for
	 * {@literal XREADGROUP}.
	 *
	 * @param noack {@code true} to disable auto-ack.
	 * @return {@code this}.
	 */
	public XReadArgs noack(boolean noack) {

		this.noack = noack;
		return this;
	}

	/**
	 * Value object representing a Stream with its offset.
	 */
	public static class StreamOffset<K> {

		final K key;

		final String id;

		private StreamOffset(K key, String id) {
			this.key = key;
			this.id = id;
		}

		/**
		 * Read all new arriving elements from the stream identified by {@code name}.
		 *
		 * @param name must not be {@code null}.
		 * @return the {@link StreamOffset} object without a specific offset.
		 */
		public static <K> StreamOffset<K> latest(K key) {

			LettuceAssert.notNull(key, "Stream must not be null");

			return new StreamOffset<>(key, "$");
		}

		/**
		 * Read all new arriving elements from the stream identified by {@code name}
		 * with ids greater than the last one consumed by the consumer group.
		 *
		 * @param name must not be {@code null}.
		 * @return the {@link StreamOffset} object without a specific offset.
		 */
		public static <K> StreamOffset<K> lastConsumed(K key) {

			LettuceAssert.notNull(key, "Stream must not be null");

			return new StreamOffset<>(key, ">");
		}

		/**
		 * Read all arriving elements from the stream identified by {@code name}
		 * starting at {@code offset}.
		 *
		 * @param name   must not be {@code null}.
		 * @param offset the stream offset.
		 * @return the {@link StreamOffset} object without a specific offset.
		 */
		public static <K> StreamOffset<K> from(K key, String id) {

			LettuceAssert.notNull(key, "Stream must not be null");
			LettuceAssert.notEmpty(id, "Offset must not be empty");

			return new StreamOffset<>(key, id);
		}

		public K getKey() {
			return key;
		}

		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			return String.format("%s:%s", key, id);
		}

	}

}
