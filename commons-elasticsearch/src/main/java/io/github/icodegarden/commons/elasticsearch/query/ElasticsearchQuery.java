package io.github.icodegarden.commons.elasticsearch.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 *
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString(callSuper = true)
public class ElasticsearchQuery<W> extends BaseQuery {

	private Map<String, Object> terms;

	private Map<String, Object> matches;
	/**
	 * 包含下界
	 */
	private Map<String, Object> rangeFroms;
	/**
	 * 包含上界
	 */
	private Map<String, Object> rangeTos;
	/**
	 * 多字段匹配
	 */
	private Map<Object, List<String>> multiMatches;
	/**
	 * 需模糊匹配，且非分词类型的字段
	 */
	private Map<String, Object> wildcards;

	private List<Object> searchAfters;// exclude

	private W with;
	
	public ElasticsearchQuery() {
		super(1, DEFAULT_PAGE_SIZE, null);
	}

	public ElasticsearchQuery(int page, int size, String orderBy, Map<String, Object> terms,
			Map<String, Object> matches, Map<String, Object> rangeFroms, Map<String, Object> rangeTos,
			Map<Object, List<String>> multiMatches, Map<String, Object> wildcards, List<Object> searchAfters, W with) {
		super(page, size, orderBy);
		this.terms = terms;
		this.matches = matches;
		this.rangeFroms = rangeFroms;
		this.rangeTos = rangeTos;
		this.multiMatches = multiMatches;
		this.wildcards = wildcards;
		this.searchAfters = searchAfters;
		this.with = with;
	}

	public ElasticsearchQuery<W> term(String name, Object value) {
		if (terms == null) {
			terms = new HashMap<String, Object>();
		}
		terms.put(name, value);
		return this;
	}

	public ElasticsearchQuery<W> terms(Map<String, Object> terms) {
		if (this.terms == null) {
			this.terms = new HashMap<String, Object>(terms);
		} else {
			this.terms.putAll(terms);
		}
		return this;
	}

	public ElasticsearchQuery<W> match(String name, Object value) {
		if (matches == null) {
			matches = new HashMap<String, Object>();
		}
		matches.put(name, value);
		return this;
	}

	public ElasticsearchQuery<W> matches(Map<String, Object> matches) {
		if (this.matches == null) {
			this.matches = new HashMap<String, Object>(matches);
		} else {
			this.matches.putAll(matches);
		}
		return this;
	}

	public ElasticsearchQuery<W> multiMatch(Object value, List<String> fieldNames) {
		if (this.multiMatches == null) {
			this.multiMatches = new HashMap<Object, List<String>>();
		}
		this.multiMatches.put(value, fieldNames);
		return this;
	}

	public ElasticsearchQuery<W> multiMatches(Map<Object, List<String>> multiMatches) {
		if (this.multiMatches == null) {
			this.multiMatches = new HashMap<Object, List<String>>(multiMatches);
		} else {
			this.multiMatches.putAll(multiMatches);
		}
		return this;
	}

	public ElasticsearchQuery<W> rangeFrom(String name, Object value) {
		if (rangeFroms == null) {
			rangeFroms = new HashMap<>();
		}
		rangeFroms.put(name, value);
		return this;
	}

	public ElasticsearchQuery<W> rangeFroms(Map<String, Object> rangeFroms) {
		if (rangeFroms == null) {
			this.rangeFroms = new HashMap<>(rangeFroms);
		} else {
			this.rangeFroms.putAll(rangeFroms);
		}
		return this;
	}

	public ElasticsearchQuery<W> rangeTo(String name, Object value) {
		if (rangeTos == null) {
			rangeTos = new HashMap<>();
		}
		rangeTos.put(name, value);
		return this;
	}

	public ElasticsearchQuery<W> rangeTos(Map<String, Object> rangeTos) {
		if (rangeTos == null) {
			this.rangeTos = new HashMap<>(rangeTos);
		} else {
			this.rangeTos.putAll(rangeTos);
		}
		return this;
	}

	public ElasticsearchQuery<W> wildcard(String name, Object value) {
		if (wildcards == null) {
			wildcards = new HashMap<String, Object>();
		}
		wildcards.put(name, value);
		return this;
	}
}