package io.github.icodegarden.commons.test.web.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.icodegarden.wing.Cacher;

@Service
public class CacherService {

	@Autowired
	private Cacher cacher;
	
	@Transactional
	public void remove1(String key) {
		System.out.println("m1");
		
		cacher.remove(key);
	}
	
	public void remove2(List<String> keys) {
		System.out.println("m2");
		
		cacher.remove(keys);
	}
}
