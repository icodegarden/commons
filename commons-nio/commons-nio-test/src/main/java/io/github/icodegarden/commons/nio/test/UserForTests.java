package io.github.icodegarden.commons.nio.test;

import java.io.Serializable;


/**
 * for test
 * @author Fangfang.Xu
 *
 */
public class UserForTests implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private int age;
	
	public UserForTests() {//for 外部的序列化框架，JDK方式不需要这个
	}
	
	public UserForTests(String name, int age) {
		super();
		this.name = name;
		this.age = age;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + age;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserForTests other = (UserForTests) obj;
		if (age != other.age)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "User [name=" + name + ", age=" + age + "]";
	}
	
}
