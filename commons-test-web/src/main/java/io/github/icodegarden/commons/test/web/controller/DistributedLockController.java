package io.github.icodegarden.commons.test.web.controller;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcLock;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisLock;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisLockMapper;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisReadWriteLockMapper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class DistributedLockController {

	@Autowired
	MysqlMybatisLockMapper mapper;
	@Autowired
	MysqlMybatisReadWriteLockMapper readWriteLockMapper;
	@Autowired
	DataSource dataSource;

	@GetMapping("lock/mysqlJdbc")
	public ResponseEntity<?> mysqlJdbc() {
		MysqlJdbcLock lock = new MysqlJdbcLock(dataSource, "abc", 5L);
		boolean b = lock.acquire(1);
		return ResponseEntity.ok(b);
	}

	@GetMapping("lock/mysqlMybatis")
	public ResponseEntity<?> mysqlMybatis() {
		MysqlMybatisLock lock = new MysqlMybatisLock(mapper, "abc", 5L);
		boolean b = lock.acquire(1);
		return ResponseEntity.ok(b);
	}
}
