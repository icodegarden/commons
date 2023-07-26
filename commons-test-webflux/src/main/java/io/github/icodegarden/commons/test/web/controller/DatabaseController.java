package io.github.icodegarden.commons.test.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.dao.OptimizeTableResults;
import io.github.icodegarden.commons.mybatis.dao.MysqlMybatisDatabase;
import io.github.icodegarden.commons.springboot.security.SecurityUtils;
import io.github.icodegarden.commons.springboot.web.util.ReactiveWebUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class DatabaseController {

	@Autowired
	MysqlMybatisDatabase mysqlMybatisDatabase;

	@GetMapping("database/mysqlMybatis")
	public ResponseEntity<?> mysqlMybatis() {
		String version = mysqlMybatisDatabase.version();
		List<String> listTables = mysqlMybatisDatabase.listTables();
		long countTable = mysqlMybatisDatabase.countTable(listTables.get(0));
		long countTable2 = mysqlMybatisDatabase.countTable(listTables.get(1));
		OptimizeTableResults optimizeTable = mysqlMybatisDatabase.optimizeTable(listTables.get(0));
		boolean errorInMysql = optimizeTable.isErrorInMysql();

		String userId = SecurityUtils.getUserId();
		System.out.println(userId);
		
		System.out.println(ReactiveWebUtils.getExchange().getAttributes());
		
		return ResponseEntity.ok("ok");
	}
}
