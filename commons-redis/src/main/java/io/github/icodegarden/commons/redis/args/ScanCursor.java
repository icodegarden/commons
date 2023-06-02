package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class ScanCursor {

	private Object cursor;

	private boolean finished;

	public ScanCursor(byte[] cursor) {
		this.cursor = cursor;
	}
	
	protected ScanCursor(Object cursor, boolean finished) {
		this.cursor = cursor;
		this.finished = finished;
	}

}
