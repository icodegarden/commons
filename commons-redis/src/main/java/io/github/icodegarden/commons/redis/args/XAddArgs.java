package io.github.icodegarden.commons.redis.args;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class XAddArgs {

	private String id;

	private Long maxlen;

	private boolean approximateTrimming;

	private boolean exactTrimming;

	private boolean nomkstream;

	private String minid;

	private Long limit;

	/**
	 * Specify the message {@code id}.
	 *
	 * @param id must not be {@code null}.
	 * @return {@code this}
	 */
	public XAddArgs id(String id) {

		Assert.notNull(id, "Id must not be null");

		this.id = id;
		return this;
	}

	/**
	 * Limit stream to {@code maxlen} entries.
	 *
	 * @param maxlen number greater 0.
	 * @return {@code this}
	 */
	public XAddArgs maxlen(long maxlen) {

		Assert.isTrue(maxlen > 0, "Maxlen must be greater 0");

		this.maxlen = maxlen;
		return this;
	}

	/**
	 * Limit stream entries by message Id.
	 *
	 * @param minid the oldest ID in the stream will be exactly the minimum between
	 *              its original oldest ID and the specified threshold.
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs minId(String minid) {

		Assert.notNull(minid, "minId must not be null");

		this.minid = minid;
		return this;
	}

	/**
	 * The maximum number of entries to trim.
	 *
	 * @param limit has meaning only if {@link #approximateTrimming `~`} was set.
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs limit(long limit) {

		Assert.isTrue(limit > 0, "Limit must be greater 0");

		this.limit = limit;
		return this;
	}

	/**
	 * Apply efficient trimming for capped streams using the {@code ~} flag.
	 *
	 * @return {@code this}
	 */
	public XAddArgs approximateTrimming() {
		return approximateTrimming(true);
	}

	/**
	 * Apply efficient trimming for capped streams using the {@code ~} flag.
	 *
	 * @param approximateTrimming {@code true} to apply efficient radix node
	 *                            trimming.
	 * @return {@code this}
	 */
	public XAddArgs approximateTrimming(boolean approximateTrimming) {

		this.approximateTrimming = approximateTrimming;
		return this;
	}

	/**
	 * Apply exact trimming for capped streams using the {@code =} flag.
	 *
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs exactTrimming() {
		return exactTrimming(true);
	}

	/**
	 * Apply exact trimming for capped streams using the {@code =} flag.
	 *
	 * @param exactTrimming {@code true} to apply exact radix node trimming.
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs exactTrimming(boolean exactTrimming) {

		this.exactTrimming = exactTrimming;
		return this;
	}

	/**
	 * Do add the message if the stream does not already exist.
	 *
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs nomkstream() {
		return nomkstream(true);
	}

	/**
	 * Do add the message if the stream does not already exist.
	 *
	 * @param nomkstream {@code true} to not create a stream if it does not already
	 *                   exist.
	 * @return {@code this}
	 * @since 6.1
	 */
	public XAddArgs nomkstream(boolean nomkstream) {

		this.nomkstream = nomkstream;
		return this;
	}

}