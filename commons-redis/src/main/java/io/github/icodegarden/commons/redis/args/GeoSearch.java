package io.github.icodegarden.commons.redis.args;

import org.springframework.util.Assert;

import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public final class GeoSearch {

	/**
	 * Create a {@link GeoRef} from a Geo set {@code member}.
	 *
	 * @param member the Geo set member to use as search reference starting point.
	 * @return the {@link GeoRef}.
	 */
	public static <K> GeoRef<K> fromMember(K member) {
		Assert.notNull(member, "Reference member must not be null");
		return new FromMember<>(member);
	}

	/**
	 * Create a {@link GeoRef} from WGS84 coordinates {@code longitude} and
	 * {@code latitude}.
	 *
	 * @param longitude the longitude coordinate according to WGS84.
	 * @param latitude  the latitude coordinate according to WGS84.
	 * @return the {@link GeoRef}.
	 */
	public static <K> GeoRef<K> fromCoordinates(double longitude, double latitude) {
		return (GeoRef<K>) new FromCoordinates(longitude, latitude);
	}

	/**
	 * Create a {@link GeoPredicate} by specifying a radius {@code distance} and
	 * {@link GeoArgs.Unit}.
	 *
	 * @param distance the radius.
	 * @param unit     size unit.
	 * @return the {@link GeoPredicate} for the specified radius.
	 */
	public static GeoPredicate byRadius(double distance, GeoArgs.Unit unit) {
		return new Radius(distance, unit);
	}

	/**
	 * Create a {@link GeoPredicate} by specifying a box of the size {@code width},
	 * {@code height} and {@link GeoArgs.Unit}.
	 *
	 * @param width  box width.
	 * @param height box height.
	 * @param unit   size unit.
	 * @return the {@link GeoPredicate} for the specified box.
	 */
	public static GeoPredicate byBox(double width, double height, GeoArgs.Unit unit) {
		return new Box(width, height, unit);
	}

	/**
	 * Geo reference specifying a search starting point.
	 *
	 * @param <K>
	 */
	public interface GeoRef<K> {

	}
	
	@Getter
	public static class FromMember<K> implements GeoRef<K> {

		final K member;

		public FromMember(K member) {
			this.member = member;
		}
	}

	@Getter
	public static class FromCoordinates implements GeoRef<byte[]> {

		final double longitude, latitude;

		public FromCoordinates(double longitude, double latitude) {
			this.longitude = longitude;
			this.latitude = latitude;
		}
	}

	/**
	 * Geo predicate specifying a search scope.
	 */
	public interface GeoPredicate {

	}

	@Getter
	public static class Radius implements GeoPredicate {

		final double distance;

		final GeoArgs.Unit unit;

		public Radius(double distance, GeoArgs.Unit unit) {
			this.distance = distance;
			this.unit = unit;
		}
	}
	@Getter
	public static class Box implements GeoPredicate {

		final double width, height;

		final GeoArgs.Unit unit;

		public Box(double width, double height, GeoArgs.Unit unit) {
			this.width = width;
			this.height = height;
			this.unit = unit;
		}
	}

}
