package io.github.icodegarden.beecomb.test.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.icodegarden.commons.springboot.security.SecurityUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Validated
@RestController
public class Test4GatewayPreAuthenticatedAuthenticationFilterController {

	@GetMapping("api/v1/test4userAuth")
	public ResponseEntity<?> test4userAuth() {
		Assert.hasText(SecurityUtils.getUserId(), "userId");
		Assert.hasText(SecurityUtils.getUsername(), "username");

		Assert.isTrue(SecurityUtils.getUserId().equals("userId1"), "userId eq");
		Assert.isTrue(SecurityUtils.getUsername().equals("username1"), "username eq");

		return ResponseEntity.ok().build();
	}

	@GetMapping("openapi/v1/test4appAuth")
	public ResponseEntity<?> test4appAuth() {
		Assert.hasText(SecurityUtils.getUserId(), "appId");
		Assert.hasText(SecurityUtils.getUsername(), "appname");

		Assert.isTrue(SecurityUtils.getUserId().equals("appId1"), "appId eq");
		Assert.isTrue(SecurityUtils.getUsername().equals("appname1"), "appname eq");

		return ResponseEntity.ok().build();
	}
}
