package io.github.icodegarden.commons.redis.args;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class GeoArgs {

	private boolean withcoord;

	private boolean withdist;

	private boolean withhash;

	private Long count;

	private boolean any;

	private Sort sort;

	/**
	 * Request coordinates for results.
	 *
	 * @return {@code this} {@link GeoArgs}.
	 */
	public GeoArgs withCoordinates() {

		withcoord = true;
		return this;
	}

	/**
	 * Request distance for results.
	 *
	 * @return {@code this} {@link GeoArgs}.
	 */
	public GeoArgs withDistance() {

		withdist = true;
		return this;
	}

	/**
	 * Request geohash for results.
	 *
	 * @return {@code this} {@link GeoArgs}.
	 */
	public GeoArgs withHash() {
		withhash = true;
		return this;
	}

	/**
	 * Limit results to {@code count} entries.
	 *
	 * @param count number greater 0.
	 * @return {@code this} {@link GeoArgs}.
	 */
	public GeoArgs withCount(long count) {
		return withCount(count, false);
	}

	/**
	 * Limit results to {@code count} entries.
	 *
	 * @param count number greater 0.
	 * @param any   whether to complete the command as soon as enough matches are
	 *              found, so the results may not be the ones closest to the
	 *              specified point.
	 * @return {@code this} {@link GeoArgs}.
	 * @since 6.1
	 */
	public GeoArgs withCount(long count, boolean any) {

		Assert.isTrue(count > 0, "Count must be greater 0");

		this.count = count;
		this.any = any;
		return this;
	}

	/**
	 * Sort results ascending.
	 *
	 * @return {@code this}
	 */
	public GeoArgs asc() {
		return sort(Sort.asc);
	}

	/**
	 * Sort results descending.
	 *
	 * @return {@code this}
	 */
	public GeoArgs desc() {
		return sort(Sort.desc);
	}

	/**
	 * Sort results.
	 *
	 * @param sort sort order, must not be {@code null}
	 * @return {@code this}
	 */
	public GeoArgs sort(Sort sort) {

		Assert.notNull(sort, "Sort must not be null");

		this.sort = sort;
		return this;
	}

	/**
	 * Sort order.
	 */
	public enum Sort {

		/**
		 * ascending.
		 */
		asc,

		/**
		 * descending.
		 */
		desc,

	}

	/**
	 * Supported geo unit.
	 */
	public enum Unit {

		/**
		 * meter.
		 */
		m,

		/**
		 * kilometer.
		 */
		km,

		/**
		 * feet.
		 */
		ft,

		/**
		 * mile.
		 */
		mi;

//        private final byte[] asBytes;
//
//        Unit() {
//            asBytes = name().getBytes();
//        }
//
//        @Override
//        public byte[] getBytes() {
//            return asBytes;
//        }
	}

}
