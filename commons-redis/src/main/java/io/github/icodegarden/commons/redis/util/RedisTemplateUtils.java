package io.github.icodegarden.commons.redis.util;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;

import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortArgs.Limit;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RedisTemplateUtils {

	public static SortParameters convertSortParameters(SortArgs params) {
		DefaultSortParameters sortParameters = new DefaultSortParameters();

		if (params.getBy() != null) {
			sortParameters.by(params.getBy());
		}
		if (params.getLimit() != null) {
			Limit limit = params.getLimit();
			sortParameters.limit(limit.getOffset(), limit.getCount());
		}
		if (params.getGet() != null) {
			params.getGet().forEach(get -> sortParameters.get(get));
		}
		if (params.isAsc()) {
			sortParameters.asc();
		}
		if (params.isDesc()) {
			sortParameters.desc();
		}
		if (params.isAlpha()) {
			sortParameters.alpha();
		}
		return sortParameters;
	}
}
