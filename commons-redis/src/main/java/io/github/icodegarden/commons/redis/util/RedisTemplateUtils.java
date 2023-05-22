package io.github.icodegarden.commons.redis.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;

import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.Range.Boundary;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortArgs.Limit;
import io.github.icodegarden.commons.redis.args.ZAddArgs;
import io.github.icodegarden.commons.redis.args.ZAggregateArgs;

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

	public static org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs convertZAddArgs(
			ZAddArgs params) {
		org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs zAddArgs = org.springframework.data.redis.connection.RedisZSetCommands.ZAddArgs
				.empty();
		if (params.isCh()) {
			zAddArgs.ch();
		}
		if (params.isGt()) {
			zAddArgs.gt();
		}
		if (params.isLt()) {
			zAddArgs.lt();
		}
		if (params.isNx()) {
			zAddArgs.nx();
		}
		if (params.isXx()) {
			zAddArgs.xx();
		}
		return zAddArgs;
	}

	public static org.springframework.data.redis.connection.RedisZSetCommands.Range convertRange(Range<?> range) {
		org.springframework.data.redis.connection.RedisZSetCommands.Range result = new org.springframework.data.redis.connection.RedisZSetCommands.Range();

		Boundary<?> boundary = range.getLower();

		if (boundary.isUnbounded()) {
			result.gte(null);
		} else {
			if (boundary.isIncluding()) {
				result.gte(boundary.getValue());
			} else {
				result.gt(boundary.getValue());
			}
		}

		boundary = range.getUpper();

		if (boundary.isUnbounded()) {
			result.lte(null);
		} else {
			if (boundary.isIncluding()) {
				result.lte(boundary.getValue());
			} else {
				result.lt(boundary.getValue());
			}
		}

		return result;
	}

	public static NullableTuple2<org.springframework.data.redis.connection.RedisZSetCommands.Aggregate, org.springframework.data.redis.connection.RedisZSetCommands.Weights> convertAggregateWeights(
			ZAggregateArgs params) {
		org.springframework.data.redis.connection.RedisZSetCommands.Aggregate aggregate = null;
		org.springframework.data.redis.connection.RedisZSetCommands.Weights weights = null;

		if (params.getAggregate() != null) {
			aggregate = org.springframework.data.redis.connection.RedisZSetCommands.Aggregate
					.valueOf(params.getAggregate().name());
		}
		if (params.getWeights() != null) {
			double[] arr = new double[params.getWeights().size()];
			int i = 0;
			for (Double d : params.getWeights()) {
				arr[i++] = d.doubleValue();
			}
			weights = org.springframework.data.redis.connection.RedisZSetCommands.Weights.of(arr);
		}
		return NullableTuples.of(aggregate, weights);
	}
}
