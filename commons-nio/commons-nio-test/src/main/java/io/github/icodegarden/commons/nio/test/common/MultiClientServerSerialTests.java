package io.github.icodegarden.commons.nio.test.common;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.nio.NioClient;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class MultiClientServerSerialTests extends MultiCSBaseTests {

	/**
	 * 测试多客户端对多服务端，每个客户端串行
	 */
	@Test
	public void multiClientServerSerial() throws Exception {
		nioServer1.start();
		nioServer2.start();
		Thread.sleep(500);// wait for server start
		
		int clients = 32;
		CountDownLatch countDownLatch = new CountDownLatch(clients);
		long start = System.currentTimeMillis();
		for(int i=0;i<clients;i++) {
			
			int port = i % 2 == 0 ? server1Port : server2Port;
			
			new Thread(i+"") {
				public void run() {
					NioClient nioClient = nioClient(port);			
					try {
						nioClient.connect();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
					for(int j=0;j<=100;j++) {
						String response;
						try {
							String req = UUID.randomUUID().toString();
							response = nioClient.request(req);
							assertEquals("response-"+req, response);
						} catch (Throwable e) {
							e.printStackTrace();
							System.exit(-1);
						}
					}
					try {
						nioClient.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
					countDownLatch.countDown();
				};
			}.start();
		}
		
		countDownLatch.await();
		System.out.println("total used millis:"+ (System.currentTimeMillis() - start));
		
		nioServer1.close();
		nioServer2.close();
		
		Thread.sleep(500);//wait close
	}

}
