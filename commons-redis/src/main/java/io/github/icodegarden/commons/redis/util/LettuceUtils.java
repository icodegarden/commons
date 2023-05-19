package io.github.icodegarden.commons.redis.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.MapScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.Range.Boundary;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.ScoredValue;
import io.github.icodegarden.commons.redis.args.ScoredValueScanCursor;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortArgs.Limit;
import io.github.icodegarden.commons.redis.args.ValueScanCursor;
import io.github.icodegarden.commons.redis.args.ZAddArgs;
import io.github.icodegarden.commons.redis.args.ZAggregateArgs;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanCursor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class LettuceUtils {

	public static ExpireArgs convertExpireArgs(ExpiryOption expiryOption) {
		ExpireArgs expireArgs = new ExpireArgs();

		switch (expiryOption) {
		case NX:
			expireArgs.nx();
			break;
		case XX:
			expireArgs.xx();
			break;
		case GT:
			expireArgs.gt();
			break;
		case LT:
			expireArgs.lt();
			break;
		}

		return expireArgs;
	}

	public static MigrateArgs<byte[]> convertMigrateArgs(MigrateParams params) {
		MigrateArgs<byte[]> migrateArgs = new MigrateArgs<>();
		if (params.isCopy()) {
			migrateArgs.copy();
		}
		if (params.isReplace()) {
			migrateArgs.replace();
		}

		migrateArgs.auth(params.getPassowrd());
		migrateArgs.auth2(params.getUsername(), params.getPassowrd());

		return migrateArgs;
	}

	public static RestoreArgs convertRestoreArgs(RestoreParams params) {
		RestoreArgs restoreArgs = new RestoreArgs();

		if (params.isReplace()) {
			restoreArgs.replace();
		}
		if (params.isAbsTtl()) {
			restoreArgs.absttl();
		}
		if (params.getIdleTime() != null) {
			restoreArgs.idleTime(params.getIdleTime());
		}
		if (params.getFrequency() != null) {
			restoreArgs.frequency(params.getFrequency());
		}

		return restoreArgs;
	}

	public static io.lettuce.core.KeyScanArgs convertScanArgs(ScanArgs params) {
		io.lettuce.core.KeyScanArgs scanArgs = new io.lettuce.core.KeyScanArgs();

		if (params.getMatch() != null) {
			scanArgs.match(params.getMatch());
		}
		if (params.getCount() != null) {
			scanArgs.limit(params.getCount());
		}
		return scanArgs;
	}

	public static io.lettuce.core.SortArgs convertSortArgs(SortArgs params) {
		io.lettuce.core.SortArgs sortArgs = new io.lettuce.core.SortArgs();

		if (params.getBy() != null) {
			sortArgs.by(new String(params.getBy(), StandardCharsets.UTF_8));
		}
		if (params.getLimit() != null) {
			Limit limit = params.getLimit();
			sortArgs.limit(limit.getOffset(), limit.getCount());
		}
		if (params.getGet() != null) {
			params.getGet().forEach(get -> sortArgs.get(new String(get, StandardCharsets.UTF_8)));
		}
		if (params.isAsc()) {
			sortArgs.asc();
		}
		if (params.isDesc()) {
			sortArgs.desc();
		}
		if (params.isAlpha()) {
			sortArgs.alpha();
		}
		return sortArgs;
	}

	public static <T> KeyScanCursor<T> convertKeyScanCursor(io.lettuce.core.KeyScanCursor<T> scanResult) {
		KeyScanCursor<T> keyScanCursor = new KeyScanCursor<T>(scanResult.getCursor(), scanResult.isFinished(),
				scanResult.getKeys());
		return keyScanCursor;
	}

	public static <T> MapScanCursor<T, T> convertMapScanCursor(io.lettuce.core.MapScanCursor<T, T> scanResult) {
		MapScanCursor<T, T> mapScanCursor = new MapScanCursor<T, T>(scanResult.getCursor(), scanResult.isFinished(),
				scanResult.getMap());
		return mapScanCursor;
	}

	public static <T> ValueScanCursor<T> convertValueScanCursor(io.lettuce.core.ValueScanCursor<T> scanResult) {
		ValueScanCursor<T> valueScanCursor = new ValueScanCursor<T>(scanResult.getCursor(), scanResult.isFinished(),
				scanResult.getValues());
		return valueScanCursor;
	}

	public static <T> ScoredValueScanCursor<T> convertScoredValueScanCursor(
			io.lettuce.core.ScoredValueScanCursor<T> scanResult) {
		List<ScoredValue<T>> collect = null;

		List<io.lettuce.core.ScoredValue<T>> list = scanResult.getValues();
		if (!CollectionUtils.isEmpty(list)) {
			collect = list.stream().map(tuple -> {
				return new ScoredValue<T>(tuple.getScore(), tuple.getValue());
			}).collect(Collectors.toList());
		}

		ScoredValueScanCursor<T> valueScanCursor = new ScoredValueScanCursor<>(scanResult.getCursor(),
				scanResult.isFinished(), collect);
		return valueScanCursor;
	}

	public static io.lettuce.core.GetExArgs convertGetExArgs(GetExArgs params) {
		io.lettuce.core.GetExArgs getExArgs = new io.lettuce.core.GetExArgs();
		if (params.getEx() != null) {
			getExArgs.ex(params.getEx());
		}
		if (params.getExAt() != null) {
			getExArgs.exAt(params.getExAt());
		}
		if (params.getPx() != null) {
			getExArgs.px(params.getPx());
		}
		if (params.getPxAt() != null) {
			getExArgs.pxAt(params.getPxAt());
		}
		if (params.isPersist()) {
			getExArgs.persist();
		}

		return getExArgs;
	}

	public static ScanCursor convertScanCursor(byte[] cursor) {
		ScanCursor scanCursor;

		if (Arrays.equals("0".getBytes(StandardCharsets.UTF_8), cursor)) {
			scanCursor = ScanCursor.INITIAL;// redis集群lettuce的首次得这样，不然报错
		} else {
			scanCursor = new ScanCursor();
			scanCursor.setCursor(new String(cursor, StandardCharsets.UTF_8));
		}
		return scanCursor;
	}

	public static io.lettuce.core.ZAddArgs convertZAddArgs(ZAddArgs params) {
		io.lettuce.core.ZAddArgs zAddArgs = new io.lettuce.core.ZAddArgs();
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

	public static io.lettuce.core.ZAggregateArgs convertZAggregateArgs(ZAggregateArgs params) {
		return convertZStoreArgs(params);
	}

	public static io.lettuce.core.ZStoreArgs convertZStoreArgs(ZAggregateArgs params) {
		io.lettuce.core.ZStoreArgs zStoreArgs = new io.lettuce.core.ZStoreArgs();

		if (params.getWeights() != null) {
			double[] arr = new double[params.getWeights().size()];

			int i = 0;
			for (Double d : params.getWeights()) {
				arr[i++] = d.doubleValue();
			}
			zStoreArgs.weights(arr);
		}
		if (params.getAggregate() != null) {
			if (params.getAggregate().equals(ZAggregateArgs.Aggregate.MAX)) {
				zStoreArgs.max();
			} else if (params.getAggregate().equals(ZAggregateArgs.Aggregate.MIN)) {
				zStoreArgs.min();
			} else if (params.getAggregate().equals(ZAggregateArgs.Aggregate.SUM)) {
				zStoreArgs.sum();
			}
		}
		return zStoreArgs;
	}

	private static <T> io.lettuce.core.Range.Boundary<T> convertBoundary(Boundary<T> boundary) {
		if (boundary.isUnbounded()) {
			return io.lettuce.core.Range.Boundary.unbounded();
		} else {
			if (boundary.isIncluding()) {
				return io.lettuce.core.Range.Boundary.including(boundary.getValue());
			} else {
				return io.lettuce.core.Range.Boundary.excluding(boundary.getValue());
			}
		}
	}

	public static <T> io.lettuce.core.Range<T> convertRange(Range<T> range) {
		io.lettuce.core.Range.Boundary<T> lower = LettuceUtils.convertBoundary(range.getLower());
		io.lettuce.core.Range.Boundary<T> upper = LettuceUtils.convertBoundary(range.getUpper());
		return io.lettuce.core.Range.from(lower, upper);
	}
}
