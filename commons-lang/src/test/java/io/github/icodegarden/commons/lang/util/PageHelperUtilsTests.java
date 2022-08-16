package io.github.icodegarden.commons.lang.util;

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.pagehelper.Page;

import io.github.icodegarden.commons.lang.query.NextQuerySupportPage;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class PageHelperUtilsTests {

	final int pageSize = 10;
	final int total = 12345;
	final String orderBy = "id desc";
//	final List<Integer> page1result = Arrays.asList(1,2,3,4,5,6,7,8,9,10);
	final List<Integer> page2result = Arrays.asList(11, 12, 13, 14, 15, 16, 17, 18, 19, 20);

	/**
	 * 进行count的结果
	 */
	final Page<Integer> countTruePage2 = new Page<>(2, pageSize);
	/**
	 * 不进行count的结果
	 */
	final Page<Integer> countFalsePage2 = new Page<>(2, pageSize, false);
	{
		countTruePage2.addAll(page2result);
		countTruePage2.setTotal(total);
		countTruePage2.setOrderBy(orderBy);

		countFalsePage2.addAll(page2result);
		countFalsePage2.setTotal(page2result.size());
		countFalsePage2.setOrderBy(orderBy);

		Assertions.assertThat(countTruePage2.isCount()).isTrue();
		Assertions.assertThat(countTruePage2.getTotal()).isEqualTo(total);
		Assertions.assertThat(countTruePage2.getOrderBy()).isEqualTo(orderBy);
		Assertions.assertThat(countTruePage2.getPageNum()).isEqualTo(2);
		Assertions.assertThat(countTruePage2.getPageSize()).isEqualTo(pageSize);
		// 真实的页数
		Assertions.assertThat(countTruePage2.getPages())
				.isEqualTo((int) (total / pageSize + ((total % pageSize == 0) ? 0 : 1)));
		Assertions.assertThat(countTruePage2.getResult()).isEqualTo(page2result);

		Assertions.assertThat(countFalsePage2.isCount()).isFalse();
		Assertions.assertThat(countFalsePage2.getTotal()).isEqualTo(page2result.size());
		Assertions.assertThat(countFalsePage2.getOrderBy()).isEqualTo(orderBy);
		Assertions.assertThat(countFalsePage2.getPageNum()).isEqualTo(2);
		Assertions.assertThat(countFalsePage2.getPageSize()).isEqualTo(pageSize);
		// 只有1页
		Assertions.assertThat(countFalsePage2.getPages()).isEqualTo(1);
		Assertions.assertThat(countFalsePage2.getResult()).isEqualTo(page2result);
	}

	/**
	 * 只转换类型，其他不变
	 */
	@Test
	void ofPage() throws Exception {
		Page<String> ofPage1 = PageHelperUtils.ofPage(countTruePage2, one -> one.toString());
		Assertions.assertThat(ofPage1.isCount()).isEqualTo(countTruePage2.isCount());
		Assertions.assertThat(ofPage1.getTotal()).isEqualTo(countTruePage2.getTotal());
		Assertions.assertThat(ofPage1.getOrderBy()).isEqualTo(countTruePage2.getOrderBy());
		Assertions.assertThat(ofPage1.getPageNum()).isEqualTo(countTruePage2.getPageNum());
		Assertions.assertThat(ofPage1.getPageSize()).isEqualTo(countTruePage2.getPageSize());
		Assertions.assertThat(ofPage1.getPages()).isEqualTo(countTruePage2.getPages());
		Assertions.assertThat(ofPage1.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));

		Page<String> ofPage2 = PageHelperUtils.ofPage(countFalsePage2, one -> one.toString());
		Assertions.assertThat(ofPage2.isCount()).isEqualTo(countFalsePage2.isCount());
		Assertions.assertThat(ofPage2.getTotal()).isEqualTo(countFalsePage2.getTotal());
		Assertions.assertThat(ofPage2.getOrderBy()).isEqualTo(countFalsePage2.getOrderBy());
		Assertions.assertThat(ofPage2.getPageNum()).isEqualTo(countFalsePage2.getPageNum());
		Assertions.assertThat(ofPage2.getPageSize()).isEqualTo(countFalsePage2.getPageSize());
		Assertions.assertThat(ofPage2.getPages()).isEqualTo(countFalsePage2.getPages());
		Assertions.assertThat(ofPage2.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));
	}

	/**
	 * count分页自适应计算<br>
	 * 省略其他重载方法的测试
	 */
	@Test
	void ofPageNoCountAdapt() throws Exception {
		/**
		 * 进行count的，转换类型，其他不变
		 */
		Page<String> ofPage1 = PageHelperUtils.ofPageNoCountAdapt(countTruePage2, one -> one.toString());
		Assertions.assertThat(ofPage1.isCount()).isEqualTo(countTruePage2.isCount());
		Assertions.assertThat(ofPage1.getTotal()).isEqualTo(countTruePage2.getTotal());
		Assertions.assertThat(ofPage1.getOrderBy()).isEqualTo(countTruePage2.getOrderBy());
		Assertions.assertThat(ofPage1.getPageNum()).isEqualTo(countTruePage2.getPageNum());
		Assertions.assertThat(ofPage1.getPageSize()).isEqualTo(countTruePage2.getPageSize());
		Assertions.assertThat(ofPage1.getPages()).isEqualTo(countTruePage2.getPages());
		Assertions.assertThat(ofPage1.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));

		/**
		 * 不count的，转换类型，总数和总页数按算法
		 */
		Page<String> ofPage2 = PageHelperUtils.ofPageNoCountAdapt(countFalsePage2, one -> one.toString());
		Assertions.assertThat(ofPage2.isCount()).isEqualTo(countFalsePage2.isCount());
		Assertions.assertThat(ofPage2.getTotal()).isEqualTo(PageHelperUtils.MAX_TOTAL);
		Assertions.assertThat(ofPage2.getOrderBy()).isEqualTo(countFalsePage2.getOrderBy());
		Assertions.assertThat(ofPage2.getPageNum()).isEqualTo(countFalsePage2.getPageNum());
		Assertions.assertThat(ofPage2.getPageSize()).isEqualTo(countFalsePage2.getPageSize());
		Assertions.assertThat(ofPage2.getPages()).isEqualTo(1000);
		Assertions.assertThat(ofPage2.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));
	}

	/**
	 * 只转换类型，其他不变
	 */
	@Test
	void ofNextQuerySupportPage() throws Exception {
		// 构造
		NextQuerySupportPage<Integer> p1 = PageHelperUtils.pageToNextQuerySupportPage(countTruePage2, one -> one,
				one -> one.toString());
		Assertions.assertThat(p1.hasNextPage()).isTrue();
		Assertions.assertThat(p1.getSearchAfter()).isNotNull();
		// do
		NextQuerySupportPage<String> ofPage1 = PageHelperUtils.ofNextQuerySupportPage(p1, one -> one.toString());
		Assertions.assertThat(ofPage1.isCount()).isEqualTo(countTruePage2.isCount());
		Assertions.assertThat(ofPage1.getTotal()).isEqualTo(countTruePage2.getTotal());
		Assertions.assertThat(ofPage1.getOrderBy()).isEqualTo(countTruePage2.getOrderBy());
		Assertions.assertThat(ofPage1.getPageNum()).isEqualTo(countTruePage2.getPageNum());
		Assertions.assertThat(ofPage1.getPageSize()).isEqualTo(countTruePage2.getPageSize());
		Assertions.assertThat(ofPage1.getPages()).isEqualTo(countTruePage2.getPages());
		Assertions.assertThat(ofPage1.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));

		// 构造
		NextQuerySupportPage<Integer> p2 = PageHelperUtils.pageToNextQuerySupportPage(countFalsePage2, one -> one,
				one -> one.toString());
		Assertions.assertThat(p2.hasNextPage()).isTrue();
		Assertions.assertThat(p2.getSearchAfter()).isNotNull();
		// do
		NextQuerySupportPage<String> ofPage2 = PageHelperUtils.ofNextQuerySupportPage(p2, one -> one.toString());
		Assertions.assertThat(ofPage2.isCount()).isEqualTo(countFalsePage2.isCount());
		Assertions.assertThat(ofPage2.getTotal()).isEqualTo(countFalsePage2.getTotal());
		Assertions.assertThat(ofPage2.getOrderBy()).isEqualTo(countFalsePage2.getOrderBy());
		Assertions.assertThat(ofPage2.getPageNum()).isEqualTo(countFalsePage2.getPageNum());
		Assertions.assertThat(ofPage2.getPageSize()).isEqualTo(countFalsePage2.getPageSize());
		Assertions.assertThat(ofPage2.getPages()).isEqualTo(countFalsePage2.getPages());
		Assertions.assertThat(ofPage2.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));
	}

	/**
	 * count分页自适应计算<br>
	 * 省略其他重载方法的测试
	 */
	@Test
	void ofNextQuerySupportPageNoCountAdapt() throws Exception {
		/**
		 * 进行count的，转换类型，其他不变
		 */
		// 构造
		NextQuerySupportPage<Integer> p1 = PageHelperUtils.pageToNextQuerySupportPage(countTruePage2, one -> one,
				one -> one.toString());
		Assertions.assertThat(p1.hasNextPage()).isTrue();
		Assertions.assertThat(p1.getSearchAfter()).isNotNull();

		// do
		NextQuerySupportPage<String> ofPage1 = PageHelperUtils.ofNextQuerySupportPageNoCountAdapt(p1,
				one -> one.toString());
		Assertions.assertThat(ofPage1.isCount()).isEqualTo(countTruePage2.isCount());
		Assertions.assertThat(ofPage1.getTotal()).isEqualTo(countTruePage2.getTotal());
		Assertions.assertThat(ofPage1.getOrderBy()).isEqualTo(countTruePage2.getOrderBy());
		Assertions.assertThat(ofPage1.getPageNum()).isEqualTo(countTruePage2.getPageNum());
		Assertions.assertThat(ofPage1.getPageSize()).isEqualTo(countTruePage2.getPageSize());
		Assertions.assertThat(ofPage1.getPages()).isEqualTo(countTruePage2.getPages());
		Assertions.assertThat(ofPage1.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));

		/**
		 * 不count的，转换类型，总数和总页数按算法
		 */
		// 构造
		NextQuerySupportPage<Integer> p2 = PageHelperUtils.pageToNextQuerySupportPage(countFalsePage2, one -> one,
				one -> one.toString());
		Assertions.assertThat(p2.hasNextPage()).isTrue();
		Assertions.assertThat(p2.getSearchAfter()).isNotNull();
		// do
		NextQuerySupportPage<String> ofPage2 = PageHelperUtils.ofNextQuerySupportPageNoCountAdapt(p2,
				one -> one.toString());
		Assertions.assertThat(ofPage2.isCount()).isEqualTo(countFalsePage2.isCount());
		Assertions.assertThat(ofPage2.getTotal()).isEqualTo(PageHelperUtils.MAX_TOTAL);
		Assertions.assertThat(ofPage2.getOrderBy()).isEqualTo(countFalsePage2.getOrderBy());
		Assertions.assertThat(ofPage2.getPageNum()).isEqualTo(countFalsePage2.getPageNum());
		Assertions.assertThat(ofPage2.getPageSize()).isEqualTo(countFalsePage2.getPageSize());
		Assertions.assertThat(ofPage2.getPages()).isEqualTo(1000);
		Assertions.assertThat(ofPage2.getResult())
				.isEqualTo(Arrays.asList("11", "12", "13", "14", "15", "16", "17", "18", "19", "20"));
	}
}
