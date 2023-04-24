package io.github.icodegarden.commons.redis.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;

import io.github.icodegarden.commons.redis.args.GetExArgs;
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

	public static Expiration convertExpiration(GetExArgs params) {
		Expiration expiration = null;

		if (params.getEx() != null) {
			expiration = Expiration.seconds(params.getEx());
		}
		if (params.getExAt() != null) {
			expiration = Expiration.unixTimestamp(params.getExAt(), TimeUnit.SECONDS);
		}
		if (params.getPx() != null) {
			expiration = Expiration.milliseconds(params.getPx());
		}
		if (params.getPxAt() != null) {
			expiration = Expiration.unixTimestamp(params.getPxAt(), TimeUnit.MILLISECONDS);
		}
		if (params.isPersist()) {
			expiration = Expiration.persistent();
		}
		return expiration;
	}
}
