package io.github.icodegarden.commons.lang.result;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 */
public class Result3<T1, T2, T3> extends Result2<T1, T2> {

	private static final long serialVersionUID = -4430274211524723033L;

	final T3 t3;

	Result3(boolean success, @Nullable T1 t1, @Nullable T2 t2, @Nullable T3 t3) {
		super(success, t1, t2);
		this.t3 = t3;
	}

	public T3 getT3() {
		return t3;
	}

}