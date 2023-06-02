package io.github.icodegarden.commons.lang.filter;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface TrustFilterChain<T> {

	boolean filter(T t);

	TrustFilter<T> rejectBy();

	class Default<T> implements TrustFilterChain<T> {
		private final List<TrustFilter<T>> filters;

		private TrustFilter<T> rejectBy;

		public Default(List<TrustFilter<T>> filters) {
			this.filters = filters;
		}

		@Override
		public boolean filter(T t) {
			if (filters != null && !filters.isEmpty()) {
				for (TrustFilter<T> filter : filters) {
					boolean b = filter.filter(t);
					if (!b) {
						rejectBy = filter;
						return false;
					}
				}
			}
			return true;
		}

		@Override
		public TrustFilter<T> rejectBy() {
			return rejectBy;
		}
	}
}
