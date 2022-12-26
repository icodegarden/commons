package io.github.icodegarden.commons.test.web.service;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.wing.Cacher;

@Service
public class CacherService {

	@Autowired
	private Cacher cacher;
	
	@Transactional
	public void m1() {
		System.out.println("m1");
		
		cacher.remove("abc");
	}
	
	public void m2() {
		System.out.println("m2");
		
		cacher.remove(Arrays.asList("a","b"));
	}
}
