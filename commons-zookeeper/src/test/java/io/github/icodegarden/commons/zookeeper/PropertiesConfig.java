package io.github.icodegarden.commons.zookeeper;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class PropertiesConfig {

//	public static String zkConnectString = System.getProperty("zkConnectString", "192.168.80.128:2181");
	public static final String zkConnectString = System.getProperty("zkConnectString", "127.0.0.1:2181");
}
