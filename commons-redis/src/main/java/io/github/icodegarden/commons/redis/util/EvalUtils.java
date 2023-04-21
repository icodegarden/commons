package io.github.icodegarden.commons.redis.util;

import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class EvalUtils {

	public static List<Object> ofMultiReturnType(Object obj) {
		if (obj instanceof List) {
			return (List) obj;
		}
		return Arrays.asList(obj);
	}
}
