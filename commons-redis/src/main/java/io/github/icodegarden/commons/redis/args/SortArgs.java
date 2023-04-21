package io.github.icodegarden.commons.redis.args;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class SortArgs {

	private byte[] by;

	private Limit limit;

	private List<byte[]> get;

	private boolean asc;
	private boolean desc;

	private boolean alpha;

	public SortArgs by(byte[] pattern) {
		this.by = pattern;
		return this;
	}

	public SortArgs limit(long offset, long count) {
		this.limit = new Limit(offset, count);
		return this;
	}

	public SortArgs get(byte[] pattern) {
		if (get == null) {
			get = new ArrayList<>();
		}
		get.add(pattern);
		return this;
	}

	public SortArgs asc() {
		asc = true;
		return this;
	}

	public SortArgs desc() {
		desc = true;
		return this;
	}

	public SortArgs alpha() {
		alpha = true;
		return this;
	}

	@Getter
	@ToString
	public static class Limit {

		private final Long offset;

		private final Long count;

		protected Limit(Long offset, Long count) {
			this.offset = offset;
			this.count = count;
		}
	}
}