package io.github.icodegarden.commons.redis.args;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

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
public class GetExArgs {

	private Long ex;

	private Long exAt;

	private Long px;

	private Long pxAt;

	private boolean persist = false;

	/**
	 * Set the specified expire time, in seconds.
	 *
	 * @param timeout expire time in seconds.
	 * @return {@code this} {@link GetExArgs}.
	 */
	public GetExArgs ex(long timeout) {

		this.ex = timeout;
		return this;
	}

	/**
	 * Set the specified expire time, in seconds.
	 *
	 * @param timeout expire time in seconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs ex(Duration timeout) {

		LettuceAssert.notNull(timeout, "Timeout must not be null");

		this.ex = timeout.toMillis() / 1000;
		return this;
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in seconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs exAt(long timestamp) {

		this.exAt = timestamp;
		return this;
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in seconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs exAt(Date timestamp) {

		LettuceAssert.notNull(timestamp, "Timestamp must not be null");

		return exAt(timestamp.getTime() / 1000);
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in seconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs exAt(Instant timestamp) {

		LettuceAssert.notNull(timestamp, "Timestamp must not be null");

		return exAt(timestamp.toEpochMilli() / 1000);
	}

	/**
	 * Set the specified expire time, in milliseconds.
	 *
	 * @param timeout expire time in milliseconds.
	 * @return {@code this} {@link GetExArgs}.
	 */
	public GetExArgs px(long timeout) {

		this.px = timeout;
		return this;
	}

	/**
	 * Set the specified expire time, in milliseconds.
	 *
	 * @param timeout expire time in milliseconds.
	 * @return {@code this} {@link GetExArgs}.
	 */
	public GetExArgs px(Duration timeout) {

		LettuceAssert.notNull(timeout, "Timeout must not be null");

		this.px = timeout.toMillis();
		return this;
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in milliseconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs pxAt(long timestamp) {

		this.pxAt = timestamp;
		return this;
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in milliseconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs pxAt(Date timestamp) {

		LettuceAssert.notNull(timestamp, "Timestamp must not be null");

		return pxAt(timestamp.getTime());
	}

	/**
	 * Set the specified expire at time using a posix {@code timestamp}.
	 *
	 * @param timestamp the timestamp type: posix time in milliseconds.
	 * @return {@code this} {@link GetExArgs}.
	 * @since 6.1
	 */
	public GetExArgs pxAt(Instant timestamp) {

		LettuceAssert.notNull(timestamp, "Timestamp must not be null");

		return pxAt(timestamp.toEpochMilli());
	}

	/**
	 * Remove the time to live associated with the key.
	 *
	 * @return {@code this} {@link GetExArgs}.
	 */
	public GetExArgs persist() {

		this.persist = true;
		return this;
	}

}