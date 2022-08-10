package io.github.icodegarden.commons.lang.query;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <E>
 */
public interface NextQuerySupportList<E> extends List<E> {

	/**
	 * 是否有下一页
	 * 
	 * @return
	 */
	boolean hasNextPage();

	/**
	 * 如果有下一页则可以使用的searchAfter
	 * 
	 * @return
	 */
	String getSearchAfter();

}
