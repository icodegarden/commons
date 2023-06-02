package io.github.icodegarden.commons.lang.filter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Fangfang.Xu
 *
 */
public class RegexFilter implements TrustFilter<String> {

//	private final String regex;
	private final Pattern p;

	public RegexFilter(String regex) {
//		this.regex = regex;
		this.p = Pattern.compile(regex);
	}

	@Override
	public boolean filter(String str) {
		Matcher m = p.matcher(str);
		return m.matches();
	}
}
