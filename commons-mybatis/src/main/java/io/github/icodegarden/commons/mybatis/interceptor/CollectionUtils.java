package io.github.icodegarden.commons.mybatis.interceptor;

import java.util.Collection;
import java.util.Map;

class CollectionUtils {

	/**
	 * 校验集合是否为空
	 *
	 * @param coll 入参
	 * @return boolean
	 */
	public static boolean isEmpty(Collection<?> coll) {
		return (coll == null || coll.isEmpty());
	}

	/**
	 * 校验集合是否不为空
	 *
	 * @param coll 入参
	 * @return boolean
	 */
	public static boolean isNotEmpty(Collection<?> coll) {
		return !isEmpty(coll);
	}

	/**
	 * 判断Map是否为空
	 *
	 * @param map 入参
	 * @return boolean
	 */
	public static boolean isEmpty(Map<?, ?> map) {
		return (map == null || map.isEmpty());
	}

	/**
	 * 判断Map是否不为空
	 *
	 * @param map 入参
	 * @return boolean
	 */
	public static boolean isNotEmpty(Map<?, ?> map) {
		return !isEmpty(map);
	}
}