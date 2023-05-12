package io.github.icodegarden.commons.redis.args;

import java.util.Map;

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
@ToString(callSuper = true)
public class MapScanCursor<K, V> extends ScanCursor {

	private final Map<K, V> map;

	public MapScanCursor(String cursor, boolean finished, Map<K, V> map) {
		super(cursor, finished);
		this.map = map;
	}

}