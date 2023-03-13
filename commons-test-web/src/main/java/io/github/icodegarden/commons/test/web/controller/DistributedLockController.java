package io.github.icodegarden.commons.test.web.controller;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcDistributedLock;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisDistributedLock;
import io.github.icodegarden.commons.mybatis.concurrent.lock.MysqlMybatisDistributedLockMapper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class DistributedLockController {

	@Autowired
	MysqlMybatisDistributedLockMapper mapper;
	@Autowired
	DataSource dataSource;

	@GetMapping("lock/mysqlJdbc")
	public ResponseEntity<?> mysqlJdbc() {
		MysqlJdbcDistributedLock lock = new MysqlJdbcDistributedLock(dataSource, "abc", 5L);
		boolean b = lock.acquire(1);
		return ResponseEntity.ok(b);
	}

	@GetMapping("lock/mysqlMybatis")
	public ResponseEntity<?> mysqlMybatis() {
		MysqlMybatisDistributedLock lock = new MysqlMybatisDistributedLock(mapper, "abc", 5L);
		boolean b = lock.acquire(1);
		return ResponseEntity.ok(b);
	}
}
