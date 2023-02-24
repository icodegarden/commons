package io.github.icodegarden.commons.test.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.test.web.service.ConsumerSystemService;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class ConsumerSystemController {

	@Autowired
	private ConsumerSystemService consumerSystemService;

	@GetMapping("/consumerSystems/create")
	public ResponseEntity<Mono<Object>> createConsumerSystem() {
		Mono<Object> mono = consumerSystemService.create();
		return ResponseEntity.ok(mono);
	}
}
