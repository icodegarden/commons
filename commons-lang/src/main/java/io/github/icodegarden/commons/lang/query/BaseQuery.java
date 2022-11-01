package io.github.icodegarden.commons.lang.query;

/**
 *
 * @author Fangfang.Xu
 *
 */
public abstract class BaseQuery {
	/**
	 * 仅用于方便常量用
	 */
	public static final int MAX_TOTAL_PAGES = 1000;
	/**
	 * 仅用于方便常量用
	 */
	public static final int MAX_PAGE_SIZE = 1000;
	/**
	 * 仅用于方便常量用
	 */
	public static final int MAX_TOTAL_COUNT = 10000;

	// -------------------------------------------

	public static final int DEFAULT_PAGE_SIZE = 10;

	private int page = 1;

	private int size = DEFAULT_PAGE_SIZE;

	/**
	 * 需要排序的值部分：例如mysql的 id desc ； ES的 "sort":后面部分的json [...]
	 */
	private String orderBy;

	public BaseQuery(int page, int size, String orderBy) {
		if (page <= 0) {
			page = 1;
		}
		if (size <= 0) {
			size = DEFAULT_PAGE_SIZE;
		}
		this.page = page;
		this.size = size;
		this.orderBy = orderBy;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	@Override
	public String toString() {
		return "BaseQuery [page=" + page + ", size=" + size + ", orderBy=" + orderBy + "]";
	}
}