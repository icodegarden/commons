package io.github.icodegarden.commons.redis.args;

import lombok.Getter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <V>
 */
@Getter
@ToString
public class GeoValue<V> {

	private double longitude;
	private double latitude;
	private V value;

	public GeoValue() {

	}

	public GeoValue(double longitude, double latitude, V value) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
		this.value = value;
	}

}
