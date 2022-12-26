package io.github.icodegarden.commons.test.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import io.github.icodegarden.commons.hbase.HBaseEnv;
import io.github.icodegarden.commons.kafka.reliability.ReliabilityProducer;
import io.github.icodegarden.commons.redis.RedisExecutor;
import io.github.icodegarden.commons.springboot.SpringContext;
import io.github.icodegarden.commons.test.web.service.UserService;
import io.github.icodegarden.commons.zookeeper.ZooKeeperHolder;
import io.github.icodegarden.wing.Cacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class UserController {
	
	@Autowired
	UserService userService;

	@GetMapping("user/m1")
	public ResponseEntity<?> m1() {
		userService.m1();
		return ResponseEntity.ok("ok");
	}
	@GetMapping("user/m2")
	public ResponseEntity<?> m2() {
		userService.m2();
		return ResponseEntity.ok("ok");
	}
}
