package io.github.icodegarden.commons.lang.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.lang.query.NextQuerySupportArrayList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportList;
import io.github.icodegarden.commons.lang.query.NextQuerySupportPage;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PageHelperUtils {

	/**
	 * 是否执行了分页，即是否执行了PageHelper.startPage 且 正在分页中<br>
	 * 如果分页调用已经结束，则是false，因为LocalPage已被自动remove
	 * 
	 * @return
	 */
	public static boolean isPage() {
		Page<Object> page = PageHelper.getLocalPage();
		return page != null;
	}

	/**
	 * 是否执行count，即是否执行了PageHelper.startPage 且 正在分页中 + count参数=true<br>
	 * 否则false
	 * 
	 * @return
	 */
	public static boolean isCount() {
		Page<Object> page = PageHelper.getLocalPage();
		return page != null ? page.isCount() : false;
	}

	/**
	 * 只转换类型
	 */
	public static <E, T> Page<E> ofPage(Page<T> page, Function<T, E> elementConvertor) {
		Page<E> newPage = new Page<E>(page.getPageNum(), page.getPageSize());
		newPage.setTotal(page.getTotal());
		newPage.setPages(page.getPages());
		newPage.setCount(page.isCount());
		newPage.setOrderBy(page.getOrderBy());

		convertAddAll(page, elementConvertor, newPage);

		return newPage;
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 */
	public static <E> Page<E> ofPageNoCountAdapt(Page<E> page) {
		ofPageNoCountAdapt(page, BaseQuery.MAX_TOTAL_COUNT);
		return page;
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 * 
	 * @param maxTotal 当没有进行count时，限制的最多条数，例如10000条
	 */
	public static <E> Page<E> ofPageNoCountAdapt(Page<E> page, long maxTotal) {
		noCountAdapt(page, page, maxTotal);
		return page;
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 */
	public static <E, T> Page<E> ofPageNoCountAdapt(Page<T> page, Function<T, E> elementConvertor) {
		return ofPageNoCountAdapt(page, elementConvertor, BaseQuery.MAX_TOTAL_COUNT);
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 * 
	 * @param maxTotal 当没有进行count时，限制的最多条数，例如10000条
	 */
	public static <E, T> Page<E> ofPageNoCountAdapt(Page<T> page, Function<T, E> elementConvertor, long maxTotal) {
		Page<E> newPage = new Page<E>(page.getPageNum(), page.getPageSize());
		noCountAdapt(page, newPage, maxTotal);

		convertAddAll(page, elementConvertor, newPage);

		return newPage;
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 * 
	 * 分页的按正常处理<br>
	 * 不分页但结果条数小于页大小，则总页数按当前页处理，总条数按(总页数-1)*每页大小+当前返回条数<br>
	 * 不分页但结果条数等于页大小，则按最大值处理
	 * 
	 * @param maxTotal 当没有进行count时，限制的最多条数，例如10000条
	 */
	private static void noCountAdapt(Page<?> page, Page<?> targetPage, long maxTotal) {
		if (page.isCount()) {
			targetPage.setTotal(page.getTotal());
			targetPage.setPages(page.getPages());
		} else {
			if (page.getResult().size() < page.getPageSize()) {
				targetPage.setTotal((page.getPages() - 1) * page.getPageSize() + page.getResult().size());
				targetPage.setPages(page.getPageNum());
			} else {
				targetPage.setTotal(maxTotal);
				targetPage.setPages((int) (maxTotal / page.getPageSize()));
			}
		}

		targetPage.setCount(page.isCount());
		targetPage.setOrderBy(page.getOrderBy());
	}

	private static <E, T> void convertAddAll(Page<T> page, Function<T, E> elementConvertor, Page<E> targetPage) {
		if (!page.getResult().isEmpty()) {
			List<E> list = page.getResult().stream().map(t -> {
				return elementConvertor.apply(t);
			}).collect(Collectors.toList());

			targetPage.addAll(list);
		}
	}

	// ----------------------------------------------------------

	/**
	 * Page 转 NextQuerySupportPage
	 */
	public static <E> NextQuerySupportPage<E> pageToNextQuerySupportPage(Page<E> page,
			Function<E, String> searchAfterSupplier) {
		NextQuerySupportList<E> nextQuerySupportList = NextQuerySupportArrayList.newSupportSearchAfter(page,
				searchAfterSupplier);
		return new NextQuerySupportPage<E>(page.getPageNum(), page.getPageSize(), page.getTotal(), page.isCount(),
				page.getOrderBy(), nextQuerySupportList);
	}

	/**
	 * Page 转 NextQuerySupportPage
	 */
	public static <T, E> NextQuerySupportPage<E> pageToNextQuerySupportPage(Page<T> page,
			Function<T, E> elementConvertor, Function<T, String> searchAfterSupplier) {
		NextQuerySupportList<E> nextQuerySupportList = NextQuerySupportArrayList.newSupportSearchAfter(page,
				elementConvertor, searchAfterSupplier);
		return new NextQuerySupportPage<E>(page.getPageNum(), page.getPageSize(), page.getTotal(), page.isCount(),
				page.getOrderBy(), nextQuerySupportList);
	}

	/**
	 * 只转换类型
	 */
	public static <T, E> NextQuerySupportPage<E> ofNextQuerySupportPage(NextQuerySupportPage<T> page,
			Function<T, E> elementConvertor) {
		NextQuerySupportList<E> nextQuerySupportList = NextQuerySupportArrayList.newSupportSearchAfter(page,
				elementConvertor, one -> page.getSearchAfter());

		return new NextQuerySupportPage<E>(page.getPageNum(), page.getPageSize(), page.getTotal(), page.isCount(),
				page.getOrderBy(), nextQuerySupportList);
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 */
	public static <E> NextQuerySupportPage<E> ofNextQuerySupportPageNoCountAdapt(NextQuerySupportPage<E> page) {
		return ofNextQuerySupportPageNoCountAdapt(page, BaseQuery.MAX_TOTAL_COUNT);
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 * 
	 * @param maxTotal 当没有进行count时，限制的最多条数，例如10000条
	 */
	public static <E> NextQuerySupportPage<E> ofNextQuerySupportPageNoCountAdapt(NextQuerySupportPage<E> page,
			long maxTotal) {
		noCountAdapt(page, page, maxTotal);
		return page;
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 */
	public static <E, T> NextQuerySupportPage<E> ofNextQuerySupportPageNoCountAdapt(NextQuerySupportPage<T> page,
			Function<T, E> elementConvertor) {
		return ofNextQuerySupportPageNoCountAdapt(page, elementConvertor, BaseQuery.MAX_TOTAL_COUNT);
	}

	/**
	 * <h1>自适应可能不进行count</h1>
	 * 
	 * @param maxTotal 当没有进行count时，限制的最多条数，例如10000条
	 */
	public static <E, T> NextQuerySupportPage<E> ofNextQuerySupportPageNoCountAdapt(NextQuerySupportPage<T> page,
			Function<T, E> elementConvertor, long maxTotal) {
		NextQuerySupportPage<E> newPage = ofNextQuerySupportPage(page, elementConvertor);
		noCountAdapt(page, newPage, maxTotal);

		return newPage;
	}
}