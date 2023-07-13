package io.github.icodegarden.commons.redis.args;

import java.time.Duration;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 */
@Getter
@ToString
public class XAutoClaimArgs<K> {

	private K group;

	private K consumer;

	private long minIdleTime;

	private String startId;

	private Long count;

	private boolean justid;

	public XAutoClaimArgs<K> group(K group) {

		Assert.notNull(group, "group must not be null");

		this.group = group;
		return this;
	}

	/**
	 * @param consumer
	 * @return {@code this}.
	 */
	public XAutoClaimArgs<K> consumer(K consumer) {

		Assert.notNull(consumer, "Consumer must not be null");

		this.consumer = consumer;
		return this;
	}

	/**
	 * The optional {@code count} argument, which defaults to {@code 100}, is the
	 * upper limit of the number of entries that the command attempts to claim.
	 *
	 * @param count
	 * @return {@code this}.
	 */
	public XAutoClaimArgs<K> count(long count) {

		this.count = count;
		return this;
	}

	/**
	 * The optional {@code JUSTID} argument changes the reply to return just an
	 * array of IDs of messages successfully claimed, without returning the actual
	 * message. Using this option means the retry counter is not incremented.
	 *
	 * @return {@code this}.
	 */
	public XAutoClaimArgs<K> justid() {

		this.justid = true;
		return this;
	}

	/**
	 * Return only messages that are idle for at least {@code milliseconds}.
	 *
	 * @param milliseconds min idle time.
	 * @return {@code this}.
	 */
	public XAutoClaimArgs<K> minIdleTime(long milliseconds) {

		this.minIdleTime = milliseconds;
		return this;
	}

	/**
	 * Return only messages that are idle for at least {@code minIdleTime}.
	 *
	 * @param minIdleTime min idle time.
	 * @return {@code this}.
	 */
	public XAutoClaimArgs<K> minIdleTime(Duration minIdleTime) {

		Assert.notNull(minIdleTime, "Min idle time must not be null");

		return minIdleTime(minIdleTime.toMillis());
	}

	/**
	 * Set the startId.
	 *
	 * @param startId
	 * @return
	 */
	public XAutoClaimArgs<K> startId(String startId) {

		Assert.notNull(startId, "StartId must not be null");

		this.startId = startId;
		return this;
	}

	public boolean isJustid() {
		return justid;
	}

}