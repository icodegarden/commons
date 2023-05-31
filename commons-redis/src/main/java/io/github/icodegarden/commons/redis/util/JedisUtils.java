package io.github.icodegarden.commons.redis.util;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.lang.tuple.Tuple2;
import io.github.icodegarden.commons.lang.tuple.Tuples;
import io.github.icodegarden.commons.redis.args.BitFieldArgs;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Get;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.IncrBy;
import io.github.icodegarden.commons.redis.args.BitFieldArgs.Overflow;
import io.github.icodegarden.commons.redis.args.BitPosParams;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GeoAddArgs;
import io.github.icodegarden.commons.redis.args.GeoArgs;
import io.github.icodegarden.commons.redis.args.GeoRadiusStoreArgs;
import io.github.icodegarden.commons.redis.args.GeoSearch;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;
import io.github.icodegarden.commons.redis.args.LPosParams;
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
import redis.clients.jedis.args.SortingOrder;
import redis.clients.jedis.params.GeoAddParams;
import redis.clients.jedis.params.GeoRadiusParam;
import redis.clients.jedis.params.GeoRadiusStoreParam;
import redis.clients.jedis.params.GeoSearchParam;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.params.ZAddParams;
import redis.clients.jedis.params.ZParams;
import redis.clients.jedis.params.ZParams.Aggregate;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.resps.Tuple;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JedisUtils {

	public static redis.clients.jedis.args.ExpiryOption convertExpiryOption(ExpiryOption expiryOption) {
		return redis.clients.jedis.args.ExpiryOption.valueOf(expiryOption.name());
	}

	public static redis.clients.jedis.params.MigrateParams convertMigrateParams(MigrateParams params) {
		redis.clients.jedis.params.MigrateParams migrateParams = new redis.clients.jedis.params.MigrateParams();
		if (params.isCopy()) {
			migrateParams.copy();
		}
		if (params.isReplace()) {
			migrateParams.replace();
		}
		migrateParams.auth(params.getPassowrd());
		migrateParams.auth2(params.getUsername(), params.getPassowrd());

		return migrateParams;
	}

	public static redis.clients.jedis.params.RestoreParams convertRestoreParams(RestoreParams params) {
		redis.clients.jedis.params.RestoreParams restoreParams = new redis.clients.jedis.params.RestoreParams();
		if (params.isReplace()) {
			restoreParams.replace();
		}
		if (params.isAbsTtl()) {
			restoreParams.absTtl();
		}
		if (params.getIdleTime() != null) {
			restoreParams.idleTime(params.getIdleTime());
		}
		if (params.getFrequency() != null) {
			restoreParams.frequency(params.getFrequency());
		}

		return restoreParams;
	}

	public static redis.clients.jedis.params.ScanParams convertScanParams(ScanArgs params) {
		redis.clients.jedis.params.ScanParams scanParams = new redis.clients.jedis.params.ScanParams();
		if (params.getMatch() != null) {
			scanParams.match(params.getMatch());
		}
		if (params.getCount() != null) {
			scanParams.count(params.getCount().intValue());
		}
		return scanParams;
	}

	public static SortingParams convertSortingParams(SortArgs params) {
		SortingParams sortingParams = new SortingParams();
		if (params.getBy() != null) {
			sortingParams.by(params.getBy());
		}
		if (params.getLimit() != null) {
			Limit limit = params.getLimit();
			sortingParams.limit(limit.getOffset().intValue(), limit.getCount().intValue());
		}
		if (params.getGet() != null) {
			params.getGet().forEach(get -> sortingParams.get(get));
		}
		if (params.isAsc()) {
			sortingParams.asc();
		}
		if (params.isDesc()) {
			sortingParams.desc();
		}
		if (params.isAlpha()) {
			sortingParams.alpha();
		}
		return sortingParams;
	}

	public static <T> KeyScanCursor<T> convertKeyScanCursor(ScanResult<T> scanResult) {
		KeyScanCursor<T> keyScanCursor = new KeyScanCursor<T>(scanResult.getCursor(),
				"0".equals(scanResult.getCursor()), scanResult.getResult());
		return keyScanCursor;
	}

	public static <T> MapScanCursor<T, T> convertMapScanCursor(ScanResult<Entry<T, T>> scanResult) {
		Map<T, T> map = scanResult.getResult().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

		MapScanCursor<T, T> mapScanCursor = new MapScanCursor<T, T>(scanResult.getCursor(),
				"0".equals(scanResult.getCursor()), map);
		return mapScanCursor;
	}

	public static <T> ValueScanCursor<T> convertValueScanCursor(ScanResult<T> scanResult) {
		ValueScanCursor<T> valueScanCursor = new ValueScanCursor<T>(scanResult.getCursor(),
				"0".equals(scanResult.getCursor()), scanResult.getResult());
		return valueScanCursor;
	}

	public static ScoredValueScanCursor<byte[]> convertScoredValueScanCursor(ScanResult<Tuple> scanResult) {
		List<ScoredValue<byte[]>> collect = null;

		List<Tuple> list = scanResult.getResult();
		if (!CollectionUtils.isEmpty(list)) {
			collect = list.stream().map(tuple -> {
				return new ScoredValue<byte[]>(tuple.getScore(), tuple.getBinaryElement());
			}).collect(Collectors.toList());
		}

		ScoredValueScanCursor<byte[]> valueScanCursor = new ScoredValueScanCursor<>(scanResult.getCursor(),
				"0".equals(scanResult.getCursor()), collect);
		return valueScanCursor;
	}

	public static GetExParams convertGetExParams(GetExArgs params) {
		GetExParams getExParams = new GetExParams();
		if (params.getEx() != null) {
			getExParams.ex(params.getEx());
		}
		if (params.getExAt() != null) {
			getExParams.exAt(params.getExAt());
		}
		if (params.getPx() != null) {
			getExParams.px(params.getPx());
		}
		if (params.getPxAt() != null) {
			getExParams.pxAt(params.getPxAt());
		}
		if (params.isPersist()) {
			getExParams.persist();
		}

		return getExParams;
	}

	public static redis.clients.jedis.params.LCSParams convertLCSParams(LCSParams params) {
		redis.clients.jedis.params.LCSParams lcsParams = new redis.clients.jedis.params.LCSParams();
		if (params.isLen()) {
			lcsParams.len();
		}
		if (params.isIdx()) {
			lcsParams.idx();
		}
		if (params.getMinMatchLen() != null) {
			lcsParams.minMatchLen(params.getMinMatchLen());
		}
		if (params.isWithMatchLen()) {
			lcsParams.withMatchLen();
		}
		return lcsParams;
	}

	public static LCSMatchResult convertLCSMatchResult(redis.clients.jedis.resps.LCSMatchResult lcsMatchResult) {
		List<redis.clients.jedis.resps.LCSMatchResult.MatchedPosition> matches = lcsMatchResult.getMatches();

		List<LCSMatchResult.MatchedPosition> ms = null;
		if (matches != null) {
			ms = matches.stream().map(match -> {
				redis.clients.jedis.resps.LCSMatchResult.Position a = match.getA();
				redis.clients.jedis.resps.LCSMatchResult.Position b = match.getB();

				LCSMatchResult.Position pa = new LCSMatchResult.Position(a.getStart(), a.getEnd());
				LCSMatchResult.Position pb = new LCSMatchResult.Position(b.getStart(), b.getEnd());

				return new LCSMatchResult.MatchedPosition(pa, pb, match.getMatchLen());
			}).collect(Collectors.toList());
		}

		return new LCSMatchResult(lcsMatchResult.getMatchString(), ms, lcsMatchResult.getLen());
	}

	public static redis.clients.jedis.params.LPosParams convertLPosParams(LPosParams params) {
		redis.clients.jedis.params.LPosParams lPosParams = redis.clients.jedis.params.LPosParams.lPosParams();
		if (params.getRank() != null) {
			lPosParams.rank(params.getRank());
		}
		if (params.getMaxLen() != null) {
			lPosParams.maxlen(params.getMaxLen());
		}
		return lPosParams;
	}

	public static ZAddParams convertZAddParams(ZAddArgs params) {
		ZAddParams zAddParams = new ZAddParams();
		if (params.isCh()) {
			zAddParams.ch();
		}
		if (params.isGt()) {
			zAddParams.gt();
		}
		if (params.isLt()) {
			zAddParams.lt();
		}
		if (params.isNx()) {
			zAddParams.nx();
		}
		if (params.isXx()) {
			zAddParams.xx();
		}
		return zAddParams;
	}

	public static ZParams convertZParams(ZAggregateArgs params) {
		ZParams zParams = new ZParams();

		if (params.getWeights() != null) {
			double[] arr = new double[params.getWeights().size()];

			int i = 0;
			for (Double d : params.getWeights()) {
				arr[i++] = d.doubleValue();
			}
			zParams.weights(arr);
		}
		if (params.getAggregate() != null) {
			Aggregate aggregate = ZParams.Aggregate.valueOf(params.getAggregate().name());
			zParams.aggregate(aggregate);
		}
		return zParams;
	}

	public static Tuple2<byte[], byte[]> convertMinMax(Range<? extends Number> range) {
		Boundary<? extends Number> lower = range.getLower();
		byte[] min = null;
		if (lower.isUnbounded()) {
			min = "-inf".getBytes(StandardCharsets.UTF_8);
		} else {
			if (lower.isIncluding()) {
				min = Double.toString(lower.getValue().doubleValue()).getBytes(StandardCharsets.UTF_8);
			} else {
				min = ("(" + lower.getValue().doubleValue()).getBytes(StandardCharsets.UTF_8);
			}
		}

		Boundary<? extends Number> upper = range.getUpper();
		byte[] max = null;
		if (upper.isUnbounded()) {
			min = "+inf".getBytes(StandardCharsets.UTF_8);
		} else {
			if (upper.isIncluding()) {
				max = Double.toString(upper.getValue().doubleValue()).getBytes(StandardCharsets.UTF_8);
			} else {
				max = ("(" + upper.getValue().doubleValue()).getBytes(StandardCharsets.UTF_8);
			}
		}

		return Tuples.of(min, max);
	}

	public static byte[][] convertBitFieldArgs(BitFieldArgs args) {
		List<byte[]> list = new LinkedList<byte[]>();

		if (CollectionUtils.isEmpty(args.getSubCommands())) {
			return new byte[0][];
		}

		for (io.github.icodegarden.commons.redis.args.BitFieldArgs.SubCommand sc : args.getSubCommands()) {
			if (sc instanceof BitFieldArgs.Set) {
				BitFieldArgs.Set set = (BitFieldArgs.Set) sc;

				list.add("SET".getBytes(StandardCharsets.UTF_8));

				list.add(((set.getBitFieldType().isSigned() ? "i" : "u") + set.getBitFieldType().getBits())
						.getBytes(StandardCharsets.UTF_8));

				if (set.isBitOffset()) {
					list.add(("#" + set.getOffset()).getBytes(StandardCharsets.UTF_8));
				} else {
					list.add(Integer.toString(set.getOffset()).getBytes(StandardCharsets.UTF_8));
				}

				list.add(Long.toString(set.getValue()).getBytes(StandardCharsets.UTF_8));
			}

			if (sc instanceof BitFieldArgs.Get) {
				Get get = (BitFieldArgs.Get) sc;

				list.add("GET".getBytes(StandardCharsets.UTF_8));

				list.add(((get.getBitFieldType().isSigned() ? "i" : "u") + get.getBitFieldType().getBits())
						.getBytes(StandardCharsets.UTF_8));

				if (get.isBitOffset()) {
					list.add(("#" + get.getOffset()).getBytes(StandardCharsets.UTF_8));
				} else {
					list.add(Integer.toString(get.getOffset()).getBytes(StandardCharsets.UTF_8));
				}
			}

			if (sc instanceof BitFieldArgs.IncrBy) {
				IncrBy incrBy = (BitFieldArgs.IncrBy) sc;

				list.add("INCRBY".getBytes(StandardCharsets.UTF_8));

				list.add(((incrBy.getBitFieldType().isSigned() ? "i" : "u") + incrBy.getBitFieldType().getBits())
						.getBytes(StandardCharsets.UTF_8));

				if (incrBy.isBitOffset()) {
					list.add(("#" + incrBy.getOffset()).getBytes(StandardCharsets.UTF_8));
				} else {
					list.add(Integer.toString(incrBy.getOffset()).getBytes(StandardCharsets.UTF_8));
				}

				list.add(Long.toString(incrBy.getValue()).getBytes(StandardCharsets.UTF_8));
			}

			if (sc instanceof BitFieldArgs.Overflow) {
				Overflow overflow = (BitFieldArgs.Overflow) sc;

				list.add("OVERFLOW".getBytes(StandardCharsets.UTF_8));

				list.add(overflow.getOverflowType().name().getBytes(StandardCharsets.UTF_8));
			}
		}

		return list.toArray(new byte[list.size()][]);
	}

	public static redis.clients.jedis.params.BitPosParams convertBitPosParams(BitPosParams params) {
		redis.clients.jedis.params.BitPosParams bitPosParams;

		if (params.getStart() == null) {
			bitPosParams = new redis.clients.jedis.params.BitPosParams();
		} else if (params.getEnd() == null) {
			bitPosParams = new redis.clients.jedis.params.BitPosParams(params.getStart());
		} else {
			bitPosParams = new redis.clients.jedis.params.BitPosParams(params.getStart(), params.getEnd());
		}
		return bitPosParams;
	}

	public static GeoAddParams convertGeoAddParams(GeoAddArgs args) {
		GeoAddParams geoAddParams = new GeoAddParams();
		if (args.isNx()) {
			geoAddParams.nx();
		}
		if (args.isXx()) {
			geoAddParams.xx();
		}
		if (args.isCh()) {
			geoAddParams.ch();
		}
		return geoAddParams;
	}

	public static GeoRadiusParam convertGeoRadiusParam(GeoArgs args) {
		GeoRadiusParam geoRadiusParam = new redis.clients.jedis.params.GeoRadiusParam();
		if (args.isWithcoord()) {
			geoRadiusParam.withCoord();
		}
		if (args.isWithdist()) {
			geoRadiusParam.withDist();
		}
		if (args.isWithhash()) {
			geoRadiusParam.withHash();
		}

		if (args.getCount() != null) {
			geoRadiusParam.count(args.getCount().intValue(), args.isAny());
		}

		if (args.getSort() != null) {
			SortingOrder valueOf = redis.clients.jedis.args.SortingOrder.valueOf(args.getSort().name());
			geoRadiusParam.sortingOrder(valueOf);
		}

		return geoRadiusParam;
	}

	public static Tuple2<GeoRadiusParam, GeoRadiusStoreParam> convertTuple(GeoRadiusStoreArgs<byte[]> storeArgs) {
		GeoRadiusParam geoRadiusParam = new redis.clients.jedis.params.GeoRadiusParam();

		if (storeArgs.getCount() != null) {
			geoRadiusParam.count(storeArgs.getCount().intValue());
		}
		if (storeArgs.getSort() != null) {
			SortingOrder valueOf = redis.clients.jedis.args.SortingOrder.valueOf(storeArgs.getSort().name());
			geoRadiusParam.sortingOrder(valueOf);
		}

		GeoRadiusStoreParam geoRadiusStoreParam = new redis.clients.jedis.params.GeoRadiusStoreParam();
		if (storeArgs.getStoreKey() != null) {
			geoRadiusStoreParam.store(new String(storeArgs.getStoreKey(), StandardCharsets.UTF_8));
		}
		if (storeArgs.getStoreDistKey() != null) {
			geoRadiusStoreParam.storeDist(new String(storeArgs.getStoreDistKey(), StandardCharsets.UTF_8));
		}

		return Tuples.of(geoRadiusParam, geoRadiusStoreParam);
	}

	public static GeoSearchParam convertGeoSearchParam(GeoSearch.GeoRef<?> reference,
			GeoSearch.GeoPredicate predicate) {
		GeoSearchParam geoSearchParam = new redis.clients.jedis.params.GeoSearchParam();

		if (reference instanceof GeoSearch.FromCoordinates) {
			GeoSearch.FromCoordinates fc = (GeoSearch.FromCoordinates) reference;
			geoSearchParam.fromLonLat(fc.getLongitude(), fc.getLatitude());
		} else if (reference instanceof GeoSearch.FromMember) {
			GeoSearch.FromMember<?> fm = (GeoSearch.FromMember) reference;

			String member;
			if (fm.getMember() instanceof byte[]) {
				member = new String((byte[]) fm.getMember(), StandardCharsets.UTF_8);
			} else {
				member = fm.getMember().toString();
			}

			geoSearchParam.fromMember(member);
		}

		if (predicate instanceof GeoSearch.Radius) {
			GeoSearch.Radius r = (GeoSearch.Radius) predicate;
			geoSearchParam.byRadius(r.getDistance(), redis.clients.jedis.args.GeoUnit.valueOf(r.getUnit().name()));
		} else if (predicate instanceof GeoSearch.Box) {
			GeoSearch.Box b = (GeoSearch.Box) predicate;
			geoSearchParam.byBox(b.getWidth(), b.getHeight(),
					redis.clients.jedis.args.GeoUnit.valueOf(b.getUnit().name()));
		}

		return geoSearchParam;
	}

	public static GeoSearchParam convertGeoSearchParam(GeoSearch.GeoRef<?> reference, GeoSearch.GeoPredicate predicate,
			GeoArgs args) {
		GeoSearchParam geoSearchParam = convertGeoSearchParam(reference, predicate);

		if (args.isWithcoord()) {
			geoSearchParam.withCoord();
		}
		if (args.isWithdist()) {
			geoSearchParam.withDist();
		}
		if (args.isWithhash()) {
			geoSearchParam.withHash();
		}

		if (args.getCount() != null) {
			geoSearchParam.count(args.getCount().intValue(), args.isAny());
		}

		if (args.getSort() != null) {
			SortingOrder valueOf = redis.clients.jedis.args.SortingOrder.valueOf(args.getSort().name());
			geoSearchParam.sortingOrder(valueOf);
		}

		return geoSearchParam;
	}
}
