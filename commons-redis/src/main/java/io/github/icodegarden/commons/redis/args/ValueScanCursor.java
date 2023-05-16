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
@ToString(callSuper = true)
public class ValueScanCursor<V> extends ScanCursor {

	private List<V> values;

	public ValueScanCursor(String cursor, boolean finished, List<V> values) {
		super(cursor, finished);
		this.values = values;
	}

}
