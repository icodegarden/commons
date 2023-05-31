package io.github.icodegarden.commons.redis.args;

import org.springframework.util.Assert;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 */
@Getter
@ToString
public class GeoRadiusStoreArgs<K> {

	private K storeKey;

	private K storeDistKey;

	private Long count;

	private GeoArgs.Sort sort;

	/**
	 * Store the resulting members with their location in the new Geo set
	 * {@code storeKey}. Cannot be used together with
	 * {@link #withStoreDist(Object)}.
	 *
	 * @param storeKey the destination key.
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> withStore(K storeKey) {

		Assert.notNull(storeKey, "StoreKey must not be null");

		this.storeKey = storeKey;
		return this;
	}

	/**
	 * Store the resulting members with their distance in the sorted set
	 * {@code storeKey}. Cannot be used together with {@link #withStore(Object)}.
	 *
	 * @param storeKey the destination key.
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> withStoreDist(K storeKey) {

		Assert.notNull(storeKey, "StoreKey must not be null");

		this.storeDistKey = storeKey;
		return this;
	}

	/**
	 * Limit results to {@code count} entries.
	 *
	 * @param count number greater 0.
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> withCount(long count) {

		Assert.isTrue(count > 0, "Count must be greater 0");

		this.count = count;
		return this;
	}

	/**
	 * Sort results ascending.
	 *
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> asc() {
		return sort(GeoArgs.Sort.asc);
	}

	/**
	 * Sort results descending.
	 *
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> desc() {
		return sort(GeoArgs.Sort.desc);
	}

	/**
	 * @return the key for storing results
	 */
	public K getStoreKey() {
		return storeKey;
	}

	/**
	 * @return the key for storing distance results
	 */
	public K getStoreDistKey() {
		return storeDistKey;
	}

	/**
	 * Sort results.
	 *
	 * @param sort sort order, must not be {@code null}
	 * @return {@code this} {@link GeoRadiusStoreArgs}.
	 */
	public GeoRadiusStoreArgs<K> sort(GeoArgs.Sort sort) {
		Assert.notNull(sort, "Sort must not be null");

		this.sort = sort;
		return this;
	}
}
