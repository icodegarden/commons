package io.github.icodegarden.commons.lang.query;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <E>
 */
public class NextQuerySupportArrayList<E> extends ArrayList<E> implements NextQuerySupportList<E> {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("rawtypes")
	private static final NextQuerySupportArrayList EMPTY = new NextQuerySupportArrayList();

	private boolean hasNextPage;
	private String searchAfter;

	/**
	 * 
	 * @param <E>
	 * @param source
	 * @param searchAfterSupplier last E in source
	 * @return
	 */
	public static <E> NextQuerySupportArrayList<E> newSupportSearchAfter(List<E> source,
			Function<E, String> searchAfterSupplier) {
		return newSupportSearchAfter(source, e -> e, searchAfterSupplier);
	}

	public static <E, R> NextQuerySupportArrayList<R> newSupportSearchAfter(List<E> source,
			Function<? super E, ? extends R> mapper, Function<E, String> searchAfterSupplier) {
		if (source.isEmpty()) {
			return EMPTY;
		}

		NextQuerySupportArrayList<R> newlist = source.stream().map(mapper)
				.collect(Collectors.toCollection(() -> new NextQuerySupportArrayList<R>()));

		String searchAfter = searchAfterSupplier.apply(source.get(source.size() - 1));
		newlist.searchAfter = searchAfter;
		newlist.hasNextPage = (searchAfter == null ? false : true);
		return newlist;
	}

	@SuppressWarnings("unchecked")
	public static final <E> NextQuerySupportList<E> empty() {
		return (NextQuerySupportList<E>) EMPTY;
	}

	@Override
	public boolean hasNextPage() {
		return hasNextPage;
	}

	@Override
	public String getSearchAfter() {
		return searchAfter;
	}

}
