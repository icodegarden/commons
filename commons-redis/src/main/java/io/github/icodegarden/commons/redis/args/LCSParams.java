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
public class LCSParams {

	private boolean len = false;
	private boolean idx = false;
	private Long minMatchLen;
	private boolean withMatchLen = false;

	/**
	 * When LEN is given the command returns the length of the longest common
	 * substring.
	 * 
	 * @return LCSParams
	 */
	public LCSParams len() {
		this.len = true;
		return this;
	}

	/**
	 * When IDX is given the command returns an array with the LCS length and all
	 * the ranges in both the strings, start and end offset for each string, where
	 * there are matches.
	 * 
	 * @return LCSParams
	 */
	public LCSParams idx() {
		this.idx = true;
		return this;
	}

	/**
	 * Specify the minimum match length.
	 * 
	 * @return LCSParams
	 */
	public LCSParams minMatchLen(long minMatchLen) {
		this.minMatchLen = minMatchLen;
		return this;
	}

	/**
	 * When WITHMATCHLEN is given each array representing a match will also have the
	 * length of the match.
	 * 
	 * @return LCSParams
	 */
	public LCSParams withMatchLen() {
		this.withMatchLen = true;
		return this;
	}
}
