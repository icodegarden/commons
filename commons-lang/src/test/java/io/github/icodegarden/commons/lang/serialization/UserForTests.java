package io.github.icodegarden.commons.lang.serialization;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * for test
 * 
 * @author Fangfang.Xu
 *
 */
@Data
@EqualsAndHashCode(exclude = {"createdAt"})
public class UserForTests implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int age;
	private boolean active;
	private LocalDateTime createdAt = LocalDateTime.now();
//	private ZonedDateTime createdAt = ZonedDateTime.now();
//	private Date createdAt = new Date();

	public UserForTests() {// for 外部的序列化框架，JDK方式不需要这个
	}

	public UserForTests(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}

}
