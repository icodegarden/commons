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
public class RestoreParams {

	private boolean replace;

	private boolean absTtl;

	private Long idleTime;

	private Long frequency;

	public RestoreParams replace() {
		this.replace = true;
		return this;
	}

	public RestoreParams absTtl() {
		this.absTtl = true;
		return this;
	}

	public RestoreParams idleTime(long idleTime) {
		this.idleTime = idleTime;
		return this;
	}

	public RestoreParams frequency(long frequency) {
		this.frequency = frequency;
		return this;
	}
}