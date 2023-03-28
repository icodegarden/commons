package io.github.icodegarden.commons.test.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.spec.response.InternalApiResponse;
import io.github.icodegarden.commons.lang.spec.response.OpenApiResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@RestController
public class ReactorController {

	@GetMapping("reactor/mono1")
	public ResponseEntity<Mono<List<String>>> mono1() {
		return ResponseEntity.ok(Mono.just(Arrays.asList("a", "b")));
	}

	@GetMapping("reactor/mono2")
	public ResponseEntity<Mono<InternalApiResponse<List<String>>>> mono2() {
		return ResponseEntity.ok(Mono.just(InternalApiResponse.success(Arrays.asList("a", "b"))));
	}

	@GetMapping("reactor/mono3")
	public ResponseEntity<Mono<OpenApiResponse>> mono3() {
		return ResponseEntity.ok(Mono.just(OpenApiResponse.success("a.b.c", "{\"name\":\"xff\"}")));
	}

	@GetMapping("reactor/flux0")
	public ResponseEntity<Flux<Object>> flux0() {
		HashMap<Object, Object> map1 = new HashMap<>();
		map1.put("name", "n1");
		HashMap<Object, Object> map2 = new HashMap<>();
		map2.put("name", "n2");

//		return ResponseEntity.ok(Flux.just("a", "b"));
		return ResponseEntity.ok(Flux.just(map1, map2));
	}

	@GetMapping("reactor/flux1")
	public ResponseEntity<Flux<List<String>>> flux1() {
		return ResponseEntity.ok(Flux.just(Arrays.asList("a", "b")));
	}

	@GetMapping("reactor/flux2")
	public ResponseEntity<Flux<InternalApiResponse<List<String>>>> flux2() {
		return ResponseEntity.ok(Flux.just(InternalApiResponse.success(Arrays.asList("a", "b"))));
	}

	@GetMapping("reactor/flux3")
	public ResponseEntity<Flux<OpenApiResponse>> flux3() {
		return ResponseEntity.ok(Flux.just(OpenApiResponse.success("a.b.c", "{\"name\":\"xff\"}")));
	}
}
