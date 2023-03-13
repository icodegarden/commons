package io.github.icodegarden.commons.mybatis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisLock extends DatabaseLock implements MybatisLock {

	public MysqlMybatisLock(MysqlMybatisLockMapper mapper, String name, Long expireSeconds) {
		super(mapper, name, expireSeconds);
	}
}
