package io.github.icodegarden.commons.lang.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public final class InputStreamClassLoader extends ClassLoader {

	private final String name;
	private InputStream is;
	private ClassLoader appClassLoader;

	/**
	 * 
	 * @param name 完整类名，例如 com.geely.ros.sync.util.classloader.Tester
	 * @param is
	 * @throws IOException
	 */
	public InputStreamClassLoader(String name, InputStream is) throws IOException {
		super(null);// 脱离双亲，否则无法重复加载classpath下的class
		if (is.available() == 0) {
			throw new IOException("InputStream's available must > 0");
		}
		this.name = name;
		appClassLoader = getSystemClassLoader();
		this.is = is;
	}

	/**
	 * 类加载的入口，给客户端一个友好的接口，不用指定name
	 * 
	 * @return 加载完成的类
	 */
	public Class<?> loadClass() throws ClassNotFoundException {
		return loadClass(name);
	}

	/**
	 * 客户端可以直接使用该接口创建实例，而不需要先使用 {@link #loadClass()}
	 * 
	 * @param interfaceClass 必须指定接口
	 * @param initargs       构造参数
	 * @return 新的实例
	 */
	public <T> T newInstance(Class<T> interfaceClass, Object... initargs) throws Exception {
		if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException("class " + interfaceClass.getName() + " is not a interface");
		}
		Class<?> loaded = loadClass();

		Class<?>[] parameterTypes = null;
		if (initargs != null && initargs.length > 0) {
			parameterTypes = new Class<?>[initargs.length];
			for (int i = 0; i < initargs.length; i++) {
				parameterTypes[i] = initargs[i].getClass();
			}
		}
		Constructor<?> declaredConstructor = loaded.getDeclaredConstructor(parameterTypes);
		return (T) declaredConstructor.newInstance(initargs);
	}

	/**
	 * 时序：
	 * 第1次进来：加载name指定的类，如com.geely.ros.sync.util.classloader.Tester，此时从stream中读取bytes并触发defineClass(...)<br>
	 * 第2次进来：加载依赖的接口，例如com.geely.ros.sync.util.classloader.ITester，而接口应使用已加载的class而不需要重新加载，所以进到if分支内<br>
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			if (is == null) {
//				return appClassLoader.loadClass(name);
				/**
				 * 通常用appClassLoader是可以的，因为由appClassLoader负责加载classpath下的类<br>
				 * 但在spring环境下可能是被org.springframework.boot.loader.LaunchedURLClassLoader加载而不是appClassLoader<br>
				 * 因此用Class.forName(name);
				 */
				return Class.forName(name);
			}
			byte[] b = new byte[is.available()];
			is.read(b);
			is = null;
			return defineClass(name, b, 0, b.length);
		} catch (IOException e) {
			throw new ClassNotFoundException(name, e);
		}
	}
}