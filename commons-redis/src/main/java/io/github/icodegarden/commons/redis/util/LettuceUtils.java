package io.github.icodegarden.commons.redis.util;

import java.nio.charset.StandardCharsets;

import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;
import io.github.icodegarden.commons.redis.args.SortArgs.Limit;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.RestoreArgs;

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
}
