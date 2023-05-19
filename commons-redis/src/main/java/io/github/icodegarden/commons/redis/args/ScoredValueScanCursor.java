package io.github.icodegarden.commons.redis.args;

import java.util.List;

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
public class ScoredValueScanCursor<V> extends ScanCursor {

	private final List<ScoredValue<V>> values;

	public ScoredValueScanCursor(String cursor, boolean finished, List<ScoredValue<V>> values) {
		super(cursor, finished);
		this.values = values;
	}
}