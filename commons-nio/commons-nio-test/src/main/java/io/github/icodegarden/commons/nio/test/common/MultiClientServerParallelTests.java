package io.github.icodegarden.commons.nio.test.common;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.nio.NioClient;
/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class MultiClientServerParallelTests extends MultiCSBaseTests {
	
	int everyClientLoop = 1000;
	
	/**
	 * 测试多客户端对多服务端，每个客户端并行
	 */
	@Test
	public void multiClientServerParallel() throws Exception {
		nioServer1.start();
		nioServer2.start();
		Thread.sleep(500);// wait for server start
		
		int clients = 16;
		CountDownLatch startSignal = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(clients);
		for (int i = 0; i < clients; i++) {
			
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
					int clientThreads = 16;
					CountDownLatch countDownLatch2 = new CountDownLatch(clientThreads);
					for(int i=0;i<clientThreads;i++) {
						new Thread(i+"") {
							public void run() {
								try {
									startSignal.await();
								} catch (InterruptedException e1) {
								}
								for(int j=0;j<=everyClientLoop;j++) {
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
								countDownLatch2.countDown();
							}
						}.start();
					}
					
					try {
						countDownLatch2.await();
					} catch (InterruptedException e1) {
					}
					
					try {
						nioClient.close();
					} catch (Exception e) {
						e.printStackTrace();
						System.exit(-1);
					}
					
					done.countDown();
				};
			}.start();
		}
		
		Thread.sleep(5000);
		startSignal.countDown();
		
		System.out.println("start time:"+LocalDateTime.now());
		long start = System.currentTimeMillis();
		done.await();
		System.out.println("total used millis:"+ (System.currentTimeMillis() - start));
		
		nioServer1.close();
		nioServer2.close();
		
		Thread.sleep(500);
	}
}
