package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@ToString
public class GeoCoordinate {

	private double longitude;
	private double latitude;

	public GeoCoordinate(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
}