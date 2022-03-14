package io.github.icodegarden.commons.lang.result;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T1>
 * @param <T2>
 */
public class Result2<T1, T2> extends Result1<T1> {

	private static final long serialVersionUID = -3518082018884860684L;

	final T2 t2;

	Result2(boolean success, @Nullable T1 t1, @Nullable T2 t2) {
		super(success, t1);
		this.t2 = t2;
	}

	public T2 getT2() {
		return t2;
	}

}