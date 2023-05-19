package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
@Getter
@ToString
public class ScoredValue<V> {

	private final double score;
	private final V value;

	public ScoredValue(double score, V value) {
		this.score = score;
		this.value = value;
	}

}
