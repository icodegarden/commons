package io.github.icodegarden.commons.lang.concurrent.registry;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Instance {

	/**
	 * @return 名称 例如serviceName
	 */
	String getName();
	
	String getInstanceId();
	
	String getHost();

	int getPort();
}
