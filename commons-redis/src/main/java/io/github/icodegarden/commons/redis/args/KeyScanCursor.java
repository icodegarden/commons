package io.github.icodegarden.commons.redis.args;

import java.util.List;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 */
@Getter
@ToString(callSuper = true)
public class KeyScanCursor<K> extends ScanCursor {

	private List<K> keys;

	public KeyScanCursor(String cursor, boolean finished, List<K> keys) {
		super(cursor, finished);
		this.keys = keys;
	}
}