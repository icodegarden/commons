package io.github.icodegarden.commons.lang.classloader;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Tester implements ITester {

	Integer num;

	public Tester() {
		num = -1;
	}

	public Tester(Integer i) {
		num = i;
	}

	public void m() {
		System.out.println("num=" + num);
	}

}
