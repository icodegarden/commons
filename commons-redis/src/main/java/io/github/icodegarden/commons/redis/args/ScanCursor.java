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

	private String cursor;

	private boolean finished;

	public ScanCursor(String cursor, boolean finished) {
		this.cursor = cursor;
		this.finished = finished;
	}

}
