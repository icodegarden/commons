package io.github.icodegarden.commons.lang.registry;

import java.io.Closeable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface InstanceRegistry<T extends RegisteredInstance> extends Closeable {

	/**
	 * @return Nullable
	 */
	T getRegistered();

	/**
	 * 如果还没注册则执行注册<br>
	 * 如果已经注册，则不会再次注册但返回已注册的实例
	 * 
	 * @return 已注册的实例
	 */
	T registerIfNot();

	/**
	 * 如果实例没有注册则忽略
	 */
	void deregister();

}
