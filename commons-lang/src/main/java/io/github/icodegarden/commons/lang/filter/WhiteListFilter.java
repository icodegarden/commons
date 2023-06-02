package io.github.icodegarden.commons.lang.filter;

import java.util.Collection;

/**
 * @author Fangfang.Xu
 *
 */
public class WhiteListFilter<T> implements TrustFilter<T> {

	private final Collection<T> whites;

	public WhiteListFilter(Collection<T> whites) {
		this.whites = whites;
	}

	@Override
	public boolean filter(T t) {
		return whites.contains(t);
	}
}
