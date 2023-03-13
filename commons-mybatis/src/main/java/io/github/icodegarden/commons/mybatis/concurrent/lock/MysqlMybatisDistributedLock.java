package io.github.icodegarden.commons.mybatis.concurrent.lock;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseDistributedLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlMybatisDistributedLock extends DatabaseDistributedLock implements MybatisDistributedLock {

	public MysqlMybatisDistributedLock(MysqlMybatisDistributedLockMapper mapper, String name, Long expireSeconds) {
		super(mapper, name, expireSeconds);
	}
}
