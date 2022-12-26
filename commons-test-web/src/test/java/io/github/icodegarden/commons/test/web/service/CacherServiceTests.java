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
public class CacherServiceTests {

	@Autowired
	private CacherService cacherService;
	
	@Test
	public void m1() throws Exception {
		cacherService.m1();
	}
	
	@Test
	public void m2() throws Exception {
		cacherService.m2();
	}
}