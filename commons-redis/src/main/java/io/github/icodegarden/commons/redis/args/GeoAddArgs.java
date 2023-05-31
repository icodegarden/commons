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
public class GeoAddArgs {

	private boolean nx = false;

	private boolean xx = false;

	private boolean ch = false;

	/**
	 * Don't update already existing elements. Always add new elements.
	 *
	 * @return {@code this} {@link GeoAddArgs}.
	 */
	public GeoAddArgs nx() {

		this.nx = true;
		return this;
	}

	/**
	 * Only update elements that already exist. Never add elements.
	 *
	 * @return {@code this} {@link GeoAddArgs}.
	 */
	public GeoAddArgs xx() {

		this.xx = true;
		return this;
	}

	/**
	 * Modify the return value from the number of new elements added, to the total
	 * number of elements changed.
	 *
	 * @return {@code this} {@link GeoAddArgs}.
	 */
	public GeoAddArgs ch() {

		this.ch = true;
		return this;
	}

}