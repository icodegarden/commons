package io.github.icodegarden.commons.test.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.dao.OptimizeTableResults;
import io.github.icodegarden.commons.mybatis.dao.MysqlMybatisDatabase;
import io.github.icodegarden.commons.test.web.service.CacherService;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class CacherController {
	
	@Autowired
	CacherService cacherService;
	@Autowired
	MysqlMybatisDatabase mysqlMybatisDatabase;

	@GetMapping("cacher/m1")
	public ResponseEntity<?> m1() {
		cacherService.m1();
		
		String version = mysqlMybatisDatabase.version();
		List<String> listTables = mysqlMybatisDatabase.listTables();
		long countTable = mysqlMybatisDatabase.countTable(listTables.get(0));
		long countTable2 = mysqlMybatisDatabase.countTable(listTables.get(1));
		OptimizeTableResults optimizeTable = mysqlMybatisDatabase.optimizeTable(listTables.get(0));
		boolean errorInMysql = optimizeTable.isErrorInMysql();
		return ResponseEntity.ok("ok");
	}
	
	@GetMapping("cacher/m2")
	public ResponseEntity<?> m2() {
		cacherService.m2();
		return ResponseEntity.ok("ok");
	}
}
