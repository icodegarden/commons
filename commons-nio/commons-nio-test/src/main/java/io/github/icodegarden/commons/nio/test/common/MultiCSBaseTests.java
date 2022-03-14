package io.github.icodegarden.commons.nio.test.common;


import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class MultiCSBaseTests {
	
	public static MessageHandler messageHandler = new MessageHandler() {
		@Override
		public Object reply(Object obj) {
//			System.out.println("messageHandler reply of receive:"+obj);
			return "response-"+obj;
		}
		@Override
		public void receive(Object obj) {
//			System.out.println("messageHandler receive:"+obj);
		}
	};
	
	int server1Port = 8888;
	int server2Port = 9999;
	
	NioServer nioServer1;
	NioServer nioServer2;
	
	@BeforeEach
	void before() {
		nioServer1 = nioServer1(server1Port);
		nioServer2 = nioServer2(server2Port);
	}
	
	protected abstract NioServer nioServer1(int port);
	protected abstract NioServer nioServer2(int port);
	protected abstract NioClient nioClient(int port);
	
}
