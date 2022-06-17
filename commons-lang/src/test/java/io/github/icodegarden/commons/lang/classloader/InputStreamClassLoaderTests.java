package io.github.icodegarden.commons.lang.classloader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class InputStreamClassLoaderTests {

	InputStreamClassLoader classLoader;

	/**
	 * eclipse跑单元测试时，类会被编译到target
	 */
	@BeforeEach
	public void before() throws IOException {
		FileInputStream fileInputStream = new FileInputStream(
				"target/classes/io/github/icodegarden/commons/lang/classloader/Tester.class");
		byte[] b = new byte[fileInputStream.available()];
		fileInputStream.read(b);
		fileInputStream.close();

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(b);
		classLoader = new InputStreamClassLoader("io.github.icodegarden.commons.lang.classloader.Tester",
				byteArrayInputStream);
	}

	@Test
	void absolutePath() throws Exception {
		System.out.println(new File("").getAbsolutePath());
	}

	@Test
	void loadClass() throws Exception {
		Tester obj1 = new Tester();

		Class<?> loaded = classLoader.loadClass();
		Object obj2 = loaded.newInstance();

		assertNotEquals(obj1, obj2);
		assertTrue(!Tester.class.isAssignableFrom(loaded.getClass()));
		assertTrue(!Tester.class.equals(loaded.getClass()));
		assertTrue(!(obj2 instanceof Tester));
		assertTrue(obj2 instanceof ITester);

		loaded = classLoader.loadClass();
		obj2 = loaded.newInstance();

		loaded = classLoader.loadClass();
		obj2 = loaded.newInstance();
	}

	@Test
	void newInstance() throws Exception {
		Tester obj1 = new Tester();

		ITester obj2 = classLoader.newInstance(ITester.class);

		assertNotEquals(obj1, obj2);
	}

	@Test
	void newInstance_byArgs() throws Exception {
		Tester obj1 = new Tester();

		ITester obj2 = classLoader.newInstance(ITester.class, 100);

		assertNotEquals(obj1, obj2);
	}

	@Test
	void newInstance_notInterface() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> {
			classLoader.newInstance(Tester.class);
		});
	}

	@Test
	void hotDeployment() throws Exception {
		Tester tester = new Tester();
		tester.m();

		for (int i = 0; i < 20; i++) {
			InputStreamClassLoader classLoader = new InputStreamClassLoader(
					"io.github.icodegarden.commons.lang.classloader.Tester",
					new FileInputStream("target/classes/io/github/icodegarden/commons/lang/classloader/Tester.class"));
			
			ITester obj = classLoader.newInstance(ITester.class, i * 10);

//			或
//			Class<?> cc = classLoader.loadClass();
//			ITester obj = (ITester) cc.newInstance();
			
			obj.m();

			Thread.sleep(100);
		}
	}
}