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
public class ZAddArgs {

	private boolean nx = false;

	private boolean xx = false;

	private boolean ch = false;

	private boolean lt = false;

	private boolean gt = false;

	/**
	 * Don't update already existing elements. Always add new elements.
	 *
	 * @return {@code this} {@link ZAddArgs}.
	 */
	public ZAddArgs nx() {

		this.nx = true;
		return this;
	}

	/**
	 * Only update elements that already exist. Never add elements.
	 *
	 * @return {@code this} {@link ZAddArgs}.
	 */
	public ZAddArgs xx() {

		this.xx = true;
		return this;
	}

	/**
	 * Modify the return value from the number of new elements added, to the total
	 * number of elements changed.
	 *
	 * @return {@code this} {@link ZAddArgs}.
	 */
	public ZAddArgs ch() {

		this.ch = true;
		return this;
	}

	/**
	 * Only update existing elements if the new score is greater than the current
	 * score. This flag doesn't prevent adding new elements.
	 *
	 * @return {@code this} {@link ZAddArgs}.
	 * @since 6.1
	 */
	public ZAddArgs gt() {

		this.gt = true;
		this.lt = false;
		return this;
	}

	/**
	 * Only update existing elements if the new score is less than the current
	 * score. This flag doesn't prevent adding new elements.
	 *
	 * @return {@code this} {@link ZAddArgs}.
	 * @since 6.1
	 */
	public ZAddArgs lt() {

		this.lt = true;
		this.gt = false;
		return this;
	}

}
