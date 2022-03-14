package io.github.icodegarden.commons.nio.test.common;


import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CSBaseTests {
	
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
	
	NioServer nioServer;
	NioClient nioClient;
	
	@BeforeEach
	void before() {
		nioServer = nioServer();
		nioClient = nioClient();
	}
	
	protected abstract NioServer nioServer();
	protected abstract NioClient nioClient();
	
	protected void startServer() throws Exception {
		nioServer.start();
		
		Thread.sleep(500);// wait for server start
	}
	
	protected void startClient() throws IOException {
		nioClient.connect();
	}
	
	@AfterEach
	public void after() throws Exception {
		try {
			nioClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			nioServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Thread.sleep(500);// wait for close async
	}
	
}
