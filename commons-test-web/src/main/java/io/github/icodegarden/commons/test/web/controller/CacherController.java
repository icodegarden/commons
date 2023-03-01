package io.github.icodegarden.commons.test.web.controller;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.lang.limiter.TokenBucketRateLimiter;
import io.github.icodegarden.commons.test.web.service.CacherService;
import io.github.icodegarden.wing.Cacher;
import io.github.icodegarden.wing.protect.BloomFilter;
import io.github.icodegarden.wing.protect.Filter;
import io.github.icodegarden.wing.protect.Protector;
import io.github.icodegarden.wing.protect.RateLimitProtector;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
@RestController
public class CacherController {

	@Configuration
	public static class ProtectionConfig{
		@Bean
		public Filter bloomFilter() {
			BloomFilter bloomFilter = new BloomFilter();
			bloomFilter.add("abc");// 只允许key=abc
			return bloomFilter;
		}
		
		@Bean
		public Protector rateLimitProtector() {
			/**
			 * 3秒1次入桶。当缓存不存在需要加载数据时如果频率高了就被限流
			 */
			TokenBucketRateLimiter rateLimiter = new TokenBucketRateLimiter(1, 1, Duration.ofSeconds(3));
			return new RateLimitProtector(rateLimiter);
		}
	}
	
	@Autowired
	private Cacher cacher;
	@Autowired
	private CacherService cacherService;

	@GetMapping("cacher/m1/{key}")
	public ResponseEntity<?> m1(@PathVariable String key) {
		Object object = cacher.getElseSupplier(key, () -> "xff1", 10);

		cacherService.remove1(key);
		return ResponseEntity.ok(object);
	}

	@GetMapping("cacher/m2")
	public ResponseEntity<?> m2() {
		log.info("request cacher/m2");
		
		cacherService.remove2(Arrays.asList("a", "b"));
		return ResponseEntity.ok("ok");
	}
}
