package io.github.icodegarden.commons.redis.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.redis.args.BitFieldArgs;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Get;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.IncrBy;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Overflow;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GeoAddArgs;
import io.github.icodegarden.commons.redis.args.GeoArgs;
import io.github.icodegarden.commons.redis.args.GeoRadiusStoreArgs;
import io.github.icodegarden.commons.redis.args.GeoSearch;
import io.github.icodegarden.commons.redis.args.GeoValue;
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
import io.lettuce.core.BitFieldArgs.BitFieldType;
import io.lettuce.core.BitFieldArgs.Offset;
import io.lettuce.core.BitFieldArgs.OverflowType;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.GeoArgs.Sort;
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

	public static io.lettuce.core.BitFieldArgs convertBitFieldArgs(BitFieldArgs args) {
		io.lettuce.core.BitFieldArgs bitFieldArgs = new io.lettuce.core.BitFieldArgs();

		if (CollectionUtils.isEmpty(args.getSubCommands())) {
			return bitFieldArgs;
		}

		for (io.github.icodegarden.commons.redis.args.BitFieldArgs.SubCommand sc : args.getSubCommands()) {
			if (sc instanceof BitFieldArgs.Set) {
				BitFieldArgs.Set set = (BitFieldArgs.Set) sc;

				BitFieldType bitFieldType;
				if (set.getBitFieldType().isSigned()) {
					bitFieldType = io.lettuce.core.BitFieldArgs.signed(set.getBitFieldType().getBits());
				} else {
					bitFieldType = io.lettuce.core.BitFieldArgs.unsigned(set.getBitFieldType().getBits());
				}

				Offset offset;
				if (set.isBitOffset()) {
					offset = io.lettuce.core.BitFieldArgs.typeWidthBasedOffset(set.getOffset());
				} else {
					offset = io.lettuce.core.BitFieldArgs.offset(set.getOffset());
				}

				bitFieldArgs.set(bitFieldType, offset, set.getValue());
			}

			if (sc instanceof BitFieldArgs.Get) {
				Get get = (BitFieldArgs.Get) sc;

				BitFieldType bitFieldType;
				if (get.getBitFieldType().isSigned()) {
					bitFieldType = io.lettuce.core.BitFieldArgs.signed(get.getBitFieldType().getBits());
				} else {
					bitFieldType = io.lettuce.core.BitFieldArgs.unsigned(get.getBitFieldType().getBits());
				}

				Offset offset;
				if (get.isBitOffset()) {
					offset = io.lettuce.core.BitFieldArgs.typeWidthBasedOffset(get.getOffset());
				} else {
					offset = io.lettuce.core.BitFieldArgs.offset(get.getOffset());
				}

				bitFieldArgs.get(bitFieldType, offset);
			}

			if (sc instanceof BitFieldArgs.IncrBy) {
				IncrBy incrBy = (BitFieldArgs.IncrBy) sc;

				BitFieldType bitFieldType;
				if (incrBy.getBitFieldType().isSigned()) {
					bitFieldType = io.lettuce.core.BitFieldArgs.signed(incrBy.getBitFieldType().getBits());
				} else {
					bitFieldType = io.lettuce.core.BitFieldArgs.unsigned(incrBy.getBitFieldType().getBits());
				}

				Offset offset;
				if (incrBy.isBitOffset()) {
					offset = io.lettuce.core.BitFieldArgs.typeWidthBasedOffset(incrBy.getOffset());
				} else {
					offset = io.lettuce.core.BitFieldArgs.offset(incrBy.getOffset());
				}

				bitFieldArgs.incrBy(bitFieldType, offset, incrBy.getValue());
			}

			if (sc instanceof BitFieldArgs.Overflow) {
				Overflow overflow = (BitFieldArgs.Overflow) sc;

				OverflowType overflowType = io.lettuce.core.BitFieldArgs.OverflowType
						.valueOf(overflow.getOverflowType().name());
				bitFieldArgs.overflow(overflowType);
			}
		}

		return bitFieldArgs;
	}

	public static io.lettuce.core.GeoAddArgs convertGeoAddArgs(GeoAddArgs args) {
		io.lettuce.core.GeoAddArgs geoAddArgs = new io.lettuce.core.GeoAddArgs();
		if (args.isNx()) {
			geoAddArgs.nx();
		}
		if (args.isXx()) {
			geoAddArgs.xx();
		}
		if (args.isCh()) {
			geoAddArgs.ch();
		}
		return geoAddArgs;
	}

	@SuppressWarnings("unchecked")
	public static <T> io.lettuce.core.GeoValue<T>[] convertGeoValues(List<GeoValue<byte[]>> geoValues) {
		List<io.lettuce.core.GeoValue<byte[]>> list = geoValues.stream().map(one -> {
			return io.lettuce.core.GeoValue.just(one.getLongitude(), one.getLatitude(), one.getValue());
		}).collect(Collectors.toList());
		return list.toArray(new io.lettuce.core.GeoValue[list.size()]);
	}

	public static io.lettuce.core.GeoArgs convertGeoArgs(GeoArgs args) {
		io.lettuce.core.GeoArgs geoArgs = new io.lettuce.core.GeoArgs();
		if (args.isWithcoord()) {
			geoArgs.withCoordinates();
		}
		if (args.isWithdist()) {
			geoArgs.withDistance();
		}
		if (args.isWithhash()) {
			geoArgs.withHash();
		}

		if (args.getCount() != null) {
			geoArgs.withCount(args.getCount(), args.isAny());
		}

		if (args.getSort() != null) {
			Sort valueOf = io.lettuce.core.GeoArgs.Sort.valueOf(args.getSort().name());
			geoArgs.sort(valueOf);
		}

		return geoArgs;
	}

	public static <T> io.lettuce.core.GeoRadiusStoreArgs<T> convertGeoRadiusStoreArgs(GeoRadiusStoreArgs<T> storeArgs) {
		io.lettuce.core.GeoRadiusStoreArgs<T> geoRadiusStoreArgs = new io.lettuce.core.GeoRadiusStoreArgs<>();

		if (storeArgs.getStoreKey() != null) {
			geoRadiusStoreArgs.withStore(storeArgs.getStoreKey());
		}
		if (storeArgs.getStoreDistKey() != null) {
			geoRadiusStoreArgs.withStoreDist(storeArgs.getStoreDistKey());
		}
		if (storeArgs.getCount() != null) {
			geoRadiusStoreArgs.withCount(storeArgs.getCount());
		}

		if (storeArgs.getSort() != null) {
			Sort valueOf = io.lettuce.core.GeoArgs.Sort.valueOf(storeArgs.getSort().name());
			geoRadiusStoreArgs.sort(valueOf);
		}
		return geoRadiusStoreArgs;
	}

	public static <T> io.lettuce.core.GeoSearch.GeoRef<T> convertGeoRef(GeoSearch.GeoRef<T> reference) {
		io.lettuce.core.GeoSearch.GeoRef<T> ref = null;
		if (reference instanceof GeoSearch.FromCoordinates) {
			GeoSearch.FromCoordinates fc = (GeoSearch.FromCoordinates) reference;
			ref = io.lettuce.core.GeoSearch.fromCoordinates(fc.getLongitude(), fc.getLatitude());
		} else if (reference instanceof GeoSearch.FromMember) {
			GeoSearch.FromMember<T> fm = (GeoSearch.FromMember) reference;
			ref = io.lettuce.core.GeoSearch.fromMember(fm.getMember());
		}
		return ref;
	}

	public static io.lettuce.core.GeoSearch.GeoPredicate convertGeoPredicate(GeoSearch.GeoPredicate predicate) {
		io.lettuce.core.GeoSearch.GeoPredicate geoPredicate = null;
		if (predicate instanceof GeoSearch.Radius) {
			GeoSearch.Radius r = (GeoSearch.Radius) predicate;
			geoPredicate = io.lettuce.core.GeoSearch.byRadius(r.getDistance(),
					io.lettuce.core.GeoArgs.Unit.valueOf(r.getUnit().name()));
		} else if (predicate instanceof GeoSearch.Box) {
			GeoSearch.Box b = (GeoSearch.Box) predicate;
			geoPredicate = io.lettuce.core.GeoSearch.byBox(b.getWidth(), b.getHeight(),
					io.lettuce.core.GeoArgs.Unit.valueOf(b.getUnit().name()));
		}
		return geoPredicate;
	}
}
