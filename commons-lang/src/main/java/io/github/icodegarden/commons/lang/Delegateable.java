package io.github.icodegarden.commons.lang;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Delegateable {
	/**
	 * 
	 * @return Nullable
	 */
	Delegateable getDelegator();

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
		Delegateable delegatable = getDelegator();
		if (delegatable != null) {
			return delegatable.ofType(cla);
		}
		return null;
	}
}
