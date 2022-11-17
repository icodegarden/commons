package io.github.icodegarden.commons.hbase.query;

import org.springframework.util.StringUtils;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 可以直接使用该类，因此这里含With
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
public class HBaseQuery<W> extends BaseQuery {

	private String searchAfter;// exclude
	private String searchBefore;// include

	private W with;

	public HBaseQuery() {
		super(1, DEFAULT_PAGE_SIZE, null);
	}
	
	public HBaseQuery(int page, int size, String orderBy) {
		super(page, size, orderBy);
	}
	
	public HBaseQuery(int page, int size, String orderBy, String searchAfter, String searchBefore, W with) {
		super(page, size, orderBy);
		this.searchAfter = searchAfter;
		this.searchBefore = searchBefore;
		this.with = with;
	}

	public void validate() throws IllegalArgumentException {
		if (getPage() > 1 && !StringUtils.hasText(getSearchAfter())) {
			throw new IllegalArgumentException("Invalid:searchAfter must not empty");
		}
	}

}