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
public class MigrateParams {

	private boolean copy = false;
	private boolean replace = false;
	private String username = null;
	private String passowrd = null;

	public MigrateParams() {
	}

	public MigrateParams copy() {
		this.copy = true;
		return this;
	}

	public MigrateParams replace() {
		this.replace = true;
		return this;
	}

	public MigrateParams auth(String password) {
		this.passowrd = password;
		return this;
	}

	public MigrateParams auth2(String username, String password) {
		this.username = username;
		this.passowrd = password;
		return this;
	}

}
