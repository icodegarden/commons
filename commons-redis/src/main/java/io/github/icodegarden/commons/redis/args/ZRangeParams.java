package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class ZRangeParams {

	private final byte[] min;
	private final byte[] max;

	private By by;

	private boolean rev;

	private Limit limit;

	private boolean withScores;

	public static enum By {
		BYSCORE, BYLEX;
	}

	@Getter
	public static class Limit {
		private int offset;
		private int count;

		public Limit(int offset, int count) {
			this.offset = offset;
			this.count = count;
		}
	}

	public ZRangeParams(byte[] min, byte[] max) {
		this.by = null;
		this.min = min;
		this.max = max;
	}

	public ZRangeParams rev() {
		this.rev = true;
		return this;
	}

	public ZRangeParams limit(int offset, int count) {
		this.limit = new Limit(offset, count);
		return this;
	}
}
