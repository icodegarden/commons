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
public class XTrimArgs {

	private Long maxlen;

	private boolean approximateTrimming;

	private boolean exactTrimming;

	private String minId;

	private Long limit;

	/**
	 * Limit stream to {@code maxlen} entries.
	 *
	 * @param maxlen number greater 0.
	 * @return {@code this}
	 */
	public XTrimArgs maxlen(long maxlen) {

		Assert.isTrue(maxlen >= 0, "Maxlen must be greater or equal to 0");

		this.maxlen = maxlen;
		return this;
	}

	/**
	 * Limit stream entries by message Id.
	 *
	 * @param minid the oldest ID in the stream will be exactly the minimum between
	 *              its original oldest ID and the specified threshold.
	 * @return {@code this}
	 */
	public XTrimArgs minId(String minid) {

		Assert.notNull(minid, "minId must not be null");

		this.minId = minid;
		return this;
	}

	/**
	 * The maximum number of entries to trim.
	 *
	 * @param limit has meaning only if {@link #approximateTrimming `~`} was set.
	 * @return {@code this}
	 */
	public XTrimArgs limit(long limit) {

		Assert.isTrue(limit >= 0, "Limit must be greater 0");

		this.limit = limit;
		return this;
	}

	/**
	 * Apply efficient trimming for capped streams using the {@code ~} flag.
	 *
	 * @return {@code this}
	 */
	public XTrimArgs approximateTrimming() {
		return approximateTrimming(true);
	}

	/**
	 * Apply efficient trimming for capped streams using the {@code ~} flag.
	 *
	 * @param approximateTrimming {@code true} to apply efficient radix node
	 *                            trimming.
	 * @return {@code this}
	 */
	public XTrimArgs approximateTrimming(boolean approximateTrimming) {

		this.approximateTrimming = approximateTrimming;
		return this;
	}

	/**
	 * Apply exact trimming for capped streams using the {@code =} flag.
	 *
	 * @return {@code this}
	 */
	public XTrimArgs exactTrimming() {
		return exactTrimming(true);
	}

	/**
	 * Apply exact trimming for capped streams using the {@code =} flag.
	 *
	 * @param exactTrimming {@code true} to apply exact radix node trimming.
	 * @return {@code this}
	 */
	public XTrimArgs exactTrimming(boolean exactTrimming) {

		this.exactTrimming = exactTrimming;
		return this;
	}

}
