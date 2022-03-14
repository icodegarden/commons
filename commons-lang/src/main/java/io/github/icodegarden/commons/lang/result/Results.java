package io.github.icodegarden.commons.lang.result;

import java.util.function.Function;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SuppressWarnings({ "rawtypes" })
public abstract class Results implements Function {

	public static Result of(boolean success) {
		return new Result(success);
	}

	public static <T1> Result1<T1> of(boolean success, @Nullable T1 t1) {
		return new Result1<>(success, t1);
	}

	public static <T1, T2> Result2<T1, T2> of(boolean success, @Nullable T1 t1, @Nullable T2 t2) {
		return new Result2<>(success, t1, t2);
	}

	public static <T1, T2, T3> Result3<T1, T2, T3> of(boolean success, @Nullable T1 t1, @Nullable T2 t2,
			@Nullable T3 t3) {
		return new Result3<>(success, t1, t2, t3);
	}

	public static <T1, T2, T3, T4> Result4<T1, T2, T3, T4> of(boolean success, @Nullable T1 t1, @Nullable T2 t2,
			@Nullable T3 t3, @Nullable T4 t4) {
		return new Result4<>(success, t1, t2, t3, t4);
	}

	Results() {
	}
}