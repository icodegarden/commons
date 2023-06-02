package io.github.icodegarden.commons.lang.filter;

import java.util.Collection;

/**
 * @author Fangfang.Xu
 *
 */
public class BlackListFilter<T> implements TrustFilter<T> {

	private final Collection<T> blacks;

	public BlackListFilter(Collection<T> blacks) {
		this.blacks = blacks;
	}

	@Override
	public boolean filter(T t) {
		return !blacks.contains(t);
	}
}
