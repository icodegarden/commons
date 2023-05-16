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
public class LPosParams {

	private Integer rank;
	private Integer maxLen;

	public LPosParams rank(int rank) {
		this.rank = rank;
		return this;
	}

	public LPosParams maxlen(int maxLen) {
		this.maxLen = maxLen;
		return this;
	}

}