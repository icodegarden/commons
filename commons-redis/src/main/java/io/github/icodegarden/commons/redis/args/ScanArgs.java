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
public class ScanArgs {

	private byte[] match;
	
	private Long count;

	public ScanArgs match(byte[] match) {
		this.match = match;
		return this;
	}

	public ScanArgs count(Long count) {
		this.count = count;
		return this;
	}
}