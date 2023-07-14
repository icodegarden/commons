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
public class XGroupCreateArgs {

	private boolean mkstream;

	private Long entriesRead;

	/**
	 * Make a stream if it does not exists.
	 *
	 * @param mkstream whether to apply {@literal MKSTREAM}
	 * @return {@code this}
	 */
	public XGroupCreateArgs mkstream(boolean mkstream) {

		this.mkstream = mkstream;
		return this;
	}

	/**
	 * Configure the {@literal ENTRIESREAD} argument.
	 *
	 * @param entriesRead number of read entries for lag tracking.
	 *
	 * @return {@code this}
	 * @since 6.2
	 */
	public XGroupCreateArgs entriesRead(long entriesRead) {

		this.entriesRead = entriesRead;
		return this;
	}

}
