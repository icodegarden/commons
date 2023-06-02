package io.github.icodegarden.commons.redis.util;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.connection.BitFieldSubCommands;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldGet;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldIncrBy;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldSet;
import org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldSubCommand;
import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.tuple.NullableTuple2;
import io.github.icodegarden.commons.lang.tuple.NullableTuples;
import io.github.icodegarden.commons.redis.args.BitFieldArgs;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Get;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.IncrBy;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Overflow;
import io.github.icodegarden.commons.redis.args.GeoUnit;
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

	public static BitFieldSubCommands convertBitFieldSubCommands(BitFieldArgs args) {
		List<BitFieldSubCommand> list = new LinkedList<BitFieldSubCommand>();

		if (CollectionUtils.isEmpty(args.getSubCommands())) {
			return BitFieldSubCommands.create();
		}

		List<BitFieldArgs.Overflow> ofs = args.getSubCommands().stream().filter(c -> c instanceof BitFieldArgs.Overflow)
				.map(c -> (BitFieldArgs.Overflow) c).collect(Collectors.toList());
		LinkedList<BitFieldArgs.Overflow> linkedList = new LinkedList<>(ofs);

		for (io.github.icodegarden.commons.redis.args.BitFieldArgs.SubCommand sc : args.getSubCommands()) {
			if (sc instanceof BitFieldArgs.Set) {
				BitFieldArgs.Set set = (BitFieldArgs.Set) sc;

				org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType bitFieldType;
				if (set.getBitFieldType().isSigned()) {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.signed(set.getBitFieldType().getBits());
				} else {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.unsigned(set.getBitFieldType().getBits());
				}

				org.springframework.data.redis.connection.BitFieldSubCommands.Offset offset = org.springframework.data.redis.connection.BitFieldSubCommands.Offset
						.offset(set.getOffset());
				if (set.isBitOffset()) {
					// 不用处理
				} else {
					offset = offset.multipliedByTypeLength();
				}

				BitFieldSet bitFieldSet = BitFieldSet.create(bitFieldType, offset, set.getValue());
				list.add(bitFieldSet);
			}

			if (sc instanceof BitFieldArgs.Get) {
				Get get = (BitFieldArgs.Get) sc;

				org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType bitFieldType;
				if (get.getBitFieldType().isSigned()) {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.signed(get.getBitFieldType().getBits());
				} else {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.unsigned(get.getBitFieldType().getBits());
				}

				org.springframework.data.redis.connection.BitFieldSubCommands.Offset offset = org.springframework.data.redis.connection.BitFieldSubCommands.Offset
						.offset(get.getOffset());
				if (get.isBitOffset()) {
					// 不用处理
				} else {
					offset = offset.multipliedByTypeLength();
				}

				BitFieldGet bitFieldGet = BitFieldGet.create(bitFieldType, offset);
				list.add(bitFieldGet);
			}

			if (sc instanceof BitFieldArgs.IncrBy) {
				IncrBy incrBy = (BitFieldArgs.IncrBy) sc;

				org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType bitFieldType;
				if (incrBy.getBitFieldType().isSigned()) {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.signed(incrBy.getBitFieldType().getBits());
				} else {
					bitFieldType = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldType
							.unsigned(incrBy.getBitFieldType().getBits());
				}

				org.springframework.data.redis.connection.BitFieldSubCommands.Offset offset = org.springframework.data.redis.connection.BitFieldSubCommands.Offset
						.offset(incrBy.getOffset());
				if (incrBy.isBitOffset()) {
					// 不用处理
				} else {
					offset = offset.multipliedByTypeLength();
				}

				Overflow poll = linkedList.poll();

				BitFieldIncrBy bitFieldIncrBy;
				if (poll != null) {
					Overflow overflow = (BitFieldArgs.Overflow) poll;

					org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldIncrBy.Overflow valueOf = org.springframework.data.redis.connection.BitFieldSubCommands.BitFieldIncrBy.Overflow
							.valueOf(overflow.getOverflowType().name());

					bitFieldIncrBy = BitFieldIncrBy.create(bitFieldType, offset, incrBy.getValue(), valueOf);
				} else {
					bitFieldIncrBy = BitFieldIncrBy.create(bitFieldType, offset, incrBy.getValue());
				}
				list.add(bitFieldIncrBy);
			}
		}

		return BitFieldSubCommands.create(list.toArray(new BitFieldSubCommand[list.size()]));
	}

	public static DistanceUnit convertDistanceUnit(GeoUnit unit) {
		DistanceUnit du = null;
		if (unit.equals(GeoUnit.M)) {
			du = DistanceUnit.METERS;
		} else if (unit.equals(GeoUnit.MI)) {
			du = DistanceUnit.MILES;
		} else if (unit.equals(GeoUnit.KM)) {
			du = DistanceUnit.KILOMETERS;
		} else if (unit.equals(GeoUnit.FT)) {
			du = DistanceUnit.FEET;
		}
		return du;
	}

}
