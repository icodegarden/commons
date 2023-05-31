package io.github.icodegarden.commons.redis.args;

import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
@ToString
public class GeoWithin<V> {

	private final V member;

	private final Double distance;

	private final Long geohash;

	private final GeoCoordinate coordinate;

	/**
	 * Creates a new {@link GeoWithin}.
	 *
	 * @param member      the member.
	 * @param distance    the distance, may be {@code null}.
	 * @param geohash     the geohash, may be {@code null}.
	 * @param coordinates the coordinates, may be {@code null}.
	 */
	public GeoWithin(V member, Double distance, Long geohash, GeoCoordinate coordinate) {

		this.member = member;
		this.distance = distance;
		this.geohash = geohash;
		this.coordinate = coordinate;
	}

	/**
	 * @return the member within the Geo set.
	 */
	public V getMember() {
		return member;
	}

	/**
	 * @return distance if requested otherwise {@code null}.
	 */
	public Double getDistance() {
		return distance;
	}

	/**
	 * @return geohash if requested otherwise {@code null}.
	 */
	public Long getGeohash() {
		return geohash;
	}

	/**
	 * @return coordinates if requested otherwise {@code null}.
	 */
	public GeoCoordinate getCoordinate() {
		return coordinate;
	}

}
