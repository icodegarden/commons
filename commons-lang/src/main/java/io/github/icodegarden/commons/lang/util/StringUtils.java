package io.github.icodegarden.commons.lang.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class StringUtils {
	private static final Pattern TPATTERN = Pattern.compile("[A-Z0-9]");

	/**
	 * 驼峰转xxx
	 */
	public static String camelToLower(String str) {
		return camelToLower(str, "");
	}

	/**
	 * 驼峰转xxx
	 */
	public static String camelToLower(String str, String connectStr) {
		Matcher matcher = TPATTERN.matcher(str);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, connectStr + matcher.group(0).toLowerCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}
}
