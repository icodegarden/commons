package io.github.icodegarden.commons.lang.query;

import com.github.pagehelper.Page;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <E>
 */
public class NextQuerySupportPage<E> extends Page<E> implements NextQuerySupportList<E> {

	private static final long serialVersionUID = 1L;

	private final NextQuerySupportList<E> nextQuerySupportList;

	public NextQuerySupportPage(int pageNum, int pageSize, long total, NextQuerySupportList<E> nextQuerySupportList) {
		super(pageNum, pageSize);
//		super.setPages(pages);pages会自动算
		super.setTotal(total);

		this.nextQuerySupportList = nextQuerySupportList;
		addAll(nextQuerySupportList);
	}

	@Override
	public boolean hasNextPage() {
		return nextQuerySupportList.hasNextPage();
	}

	@Override
	public String getSearchAfter() {
		return nextQuerySupportList.getSearchAfter();
	}

}
