package io.github.icodegarden.commons.test.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

	@GetMapping("cacher/m1")
	public ResponseEntity<?> m1() {
		cacherService.m1();
		return ResponseEntity.ok("ok");
	}
	@GetMapping("cacher/m2")
	public ResponseEntity<?> m2() {
		cacherService.m2();
		return ResponseEntity.ok("ok");
	}
}
