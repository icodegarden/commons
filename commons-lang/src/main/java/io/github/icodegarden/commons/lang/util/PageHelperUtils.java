package io.github.icodegarden.commons.lang.util;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.github.pagehelper.Page;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PageHelperUtils {

	public static <E, T> Page<E> ofPage(Page<T> page, Function<T, E> elementConvertor) {
		Page<E> p = new Page<E>(page.getPageNum(), page.getPageSize());
		p.setTotal(page.getTotal());
		p.setPages(page.getPages());

		if (!page.getResult().isEmpty()) {
			List<E> list = page.getResult().stream().map(t -> {
				return elementConvertor.apply(t);
			}).collect(Collectors.toList());

			p.addAll(list);
		}

		return p;
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
	public static <E> Page<E> ofPageNoCountAdapt(Page<E> page, long maxTotal) {
		if (page.isCount()) {
			return page;
		} else {
			if (page.getResult().size() < page.getPageSize()) {
				page.setTotal((page.getPages() - 1) * page.getPageSize() + page.getResult().size());
				page.setPages(page.getPageNum());
			} else {
				page.setTotal(maxTotal);
				page.setPages((int) (maxTotal / page.getPageSize()));
			}
		}
		return page;
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
	public static <E, T> Page<E> ofPageNoCountAdapt(Page<T> page, long maxTotal, Function<T, E> elementConvertor) {
		Page<E> p = new Page<E>(page.getPageNum(), page.getPageSize());
		if (page.isCount()) {
			p.setTotal(page.getTotal());
			p.setPages(page.getPages());
		} else {
			if (page.getResult().size() < page.getPageSize()) {
				p.setTotal((p.getPages() - 1) * page.getPageSize() + page.getResult().size());
				p.setPages(page.getPageNum());
			} else {
				p.setTotal(maxTotal);
				page.setPages((int) (maxTotal / page.getPageSize()));
			}
		}

		if (!page.getResult().isEmpty()) {
			List<E> list = page.getResult().stream().map(t -> {
				return elementConvertor.apply(t);
			}).collect(Collectors.toList());

			p.addAll(list);
		}

		return p;
	}
}