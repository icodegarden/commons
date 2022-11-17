package io.github.icodegarden.commons.test.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.wing.Cacher;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class CacherController {

	@Autowired
	private Cacher cacher;

	@GetMapping("api/v1/caches")
	public ResponseEntity<?> createUser() {
		String str = cacher.getElseSupplier("k1", () -> "v1", 3);
		return ResponseEntity.ok(str);
	}

}
