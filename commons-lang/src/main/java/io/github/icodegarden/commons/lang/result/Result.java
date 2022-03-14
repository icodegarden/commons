package io.github.icodegarden.commons.lang.result;

import java.io.Serializable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Result implements Serializable {

	private static final long serialVersionUID = -3518082018884860684L;

	final boolean success;

	Result(boolean success) {
		this.success = success;
	}

	public boolean isSuccess() {
		return success;
	}

}