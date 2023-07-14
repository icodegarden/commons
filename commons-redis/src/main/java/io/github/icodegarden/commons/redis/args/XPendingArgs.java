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
public class XPendingArgs<K> {

	private K group;

	private K consumer;

	private Range<String> range;

	private Limit limit;

	private Long idle;

	public XPendingArgs<K> range(Range<String> range) {

		Assert.notNull(range, "Range must not be null");

		this.range = range;
		return this;
	}

	public XPendingArgs<K> consumer(K consumer) {

		Assert.notNull(consumer, "Consumer must not be null");

		this.consumer = consumer;
		return this;
	}

	/**
	 *
	 * @param group
	 * @return
	 * @since 6.1.9
	 */
	public XPendingArgs<K> group(K group) {

		Assert.notNull(group, "Group must not be null");

		this.group = group;
		return this;
	}

	public XPendingArgs<K> limit(Limit limit) {

		Assert.notNull(limit, "Limit must not be null");

		this.limit = limit;
		return this;
	}

	/**
	 * Include only entries that are idle for {@link Duration}.
	 *
	 * @param timeout
	 * @return {@code this} {@link XPendingArgs}.
	 */
	public XPendingArgs<K> idle(Duration timeout) {

		Assert.notNull(timeout, "Timeout must not be null");

		return idle(timeout.toMillis());
	}

	/**
	 * Include only entries that are idle for {@code milliseconds}.
	 *
	 * @param milliseconds
	 * @return {@code this} {@link XPendingArgs}.
	 */
	public XPendingArgs<K> idle(long milliseconds) {
		this.idle = milliseconds;
		return this;
	}

}