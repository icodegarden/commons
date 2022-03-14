package io.github.icodegarden.commons.lang;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Delegatable {
	/**
	 * 
	 * @return Nullable
	 */
	default Delegatable getDelegatable() {
		return null;
	}

	/**
	 * 自身或delegate(并且递归)是否instanceof super
	 * 
	 * @param cla super
	 * @return
	 */
	default boolean instanceOf(Class<?> cla) {
		return ofType(cla) != null;
	}

	default <T> T ofType(Class<T> cla) {
		if (cla.isAssignableFrom(this.getClass())) {
			return (T) this;
		}
		Delegatable delegatable = getDelegatable();
		if (delegatable != null) {
			return delegatable.ofType(cla);
		}
		return null;
	}
}
