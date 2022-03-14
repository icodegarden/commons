package io.github.icodegarden.commons.lang.result;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T1>
 */
public class Result1<T1> extends Result {
	private static final long serialVersionUID = 8564367822935418344L;

	final T1 t1;

	Result1(boolean success, @Nullable T1 t1) {
		super(success);
		this.t1 = t1;
	}

	public T1 getT1() {
		return t1;
	}

}