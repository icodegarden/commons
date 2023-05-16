package io.github.icodegarden.commons.redis.util;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;
import io.github.icodegarden.commons.redis.args.LPosParams;
import io.github.icodegarden.commons.redis.args.MapScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortArgs.Limit;
import io.github.icodegarden.commons.redis.args.ValueScanCursor;
import redis.clients.jedis.params.GetExParams;
import redis.clients.jedis.params.SortingParams;
import redis.clients.jedis.resps.ScanResult;

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
}
