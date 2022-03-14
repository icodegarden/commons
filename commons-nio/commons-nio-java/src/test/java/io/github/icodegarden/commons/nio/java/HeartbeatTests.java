package io.github.icodegarden.commons.nio.java;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.github.icodegarden.commons.nio.task.ReconnectTimerTask;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HeartbeatTests {

	@Test
	public void heartbeat() throws Exception {
		/**
		 * server端设置2秒，client设置300000，检测server主动断开client的链接，client自动重连并发送消息成功
		 */
		NioServer nioServer = new JavaNioServer("s1", new InetSocketAddress("127.0.0.1", 8888), 2000,
				JavaCS.messageHandler);
		HeartbeatTimerTask heartbeatTimerTask = new HeartbeatTimerTask(300000);
		ReconnectTimerTask reconnectTimerTask = new ReconnectTimerTask(2000);
		NioClient nioClient = new JavaNioClient(new InetSocketAddress("127.0.0.1", 8888), new JavaCS().clientNioSelector,
				heartbeatTimerTask, reconnectTimerTask);

		nioServer.start();

		Thread.sleep(500);// wait for server start

		nioClient.connect();

		Thread.sleep(13000);// server 6秒时第一次idle检查通过（客户端6秒时重连），server 12秒时检测不通过断开（客户端12秒时重连)

		for (int i = 0; nioClient.isClosed() && i < 100; i++) {
			System.out.println("testHeartbeat wait client reconnect");
			Thread.sleep(100);
		}

		nioClient.send("xff666");

		String response = nioClient.request("xff");
		assertEquals("response-xff", response);

		response = nioClient.request("xff2");
		assertEquals("response-xff2", response);

		response = nioClient.request("xff3");
		assertEquals("response-xff3", response);

		nioServer.close();
		
		Thread.sleep(500);
	}

}
