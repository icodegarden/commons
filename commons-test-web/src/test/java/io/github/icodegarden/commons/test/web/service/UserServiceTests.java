package io.github.icodegarden.commons.test.web.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@SpringBootTest
public class UserServiceTests {

	@Autowired
	private UserService userService;
	
	@Test
	public void m1() throws Exception {
		userService.m1();
	}
	
	@Test
	public void m2() throws Exception {
		userService.m2();
	}
}