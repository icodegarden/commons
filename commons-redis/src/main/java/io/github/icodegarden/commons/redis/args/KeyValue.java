package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
@Getter
@ToString
public class KeyValue<K, V> {

	private final K key;

	private final V value;

	public KeyValue(K key, V value) {
		this.key = key;
		this.value = value;
	}

}