package io.github.icodegarden.commons.redis.args;

import java.util.ArrayList;
import java.util.List;

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
public class ZAggregateArgs {

	public static enum Aggregate {
		SUM, MIN, MAX
	}

	private List<Double> weights;

	private Aggregate aggregate;

	/**
	 * Specify a multiplication factor for each input sorted set.
	 *
	 * @param weights must not be {@code null}.
	 * @return {@code this} {@link ZAggregateArgs}.
	 */
	public ZAggregateArgs weights(double... weights) {

		Assert.notNull(weights, "Weights must not be null");

		this.weights = new ArrayList<>(weights.length);

		for (double weight : weights) {
			this.weights.add(weight);
		}
		return this;
	}

	/**
	 * Aggregate scores of elements existing across multiple sets by summing up.
	 *
	 * @return {@code this} {@link ZAggregateArgs}.
	 */
	public ZAggregateArgs sum() {

		this.aggregate = Aggregate.SUM;
		return this;
	}

	/**
	 * Aggregate scores of elements existing across multiple sets by using the
	 * lowest score.
	 *
	 * @return {@code this} {@link ZAggregateArgs}.
	 */
	public ZAggregateArgs min() {

		this.aggregate = Aggregate.MIN;
		return this;
	}

	/**
	 * Aggregate scores of elements existing across multiple sets by using the
	 * highest score.
	 *
	 * @return {@code this} {@link ZAggregateArgs}.
	 */
	public ZAggregateArgs max() {

		this.aggregate = Aggregate.MAX;
		return this;
	}

}
