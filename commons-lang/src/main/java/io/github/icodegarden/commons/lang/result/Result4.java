package io.github.icodegarden.commons.lang.result;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */
public class Result4<T1, T2, T3, T4> extends Result3<T1, T2, T3> {

	private static final long serialVersionUID = -4898704078143033129L;

	final T4 t4;

	Result4(boolean success, @Nullable T1 t1, @Nullable T2 t2, @Nullable T3 t3, @Nullable T4 t4) {
		super(success, t1, t2, t3);
		this.t4 = t4;
	}

	public T4 getT4() {
		return t4;
	}

}