package io.github.icodegarden.commons.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

public class ConnectionFactoryUtil {

	public static ConnectionFactory getConnectionFactory(String host){
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		return factory;
	}
	
	public static ConnectionFactory getConnectionFactory(String host,String username,String password){
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(host);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}
}
