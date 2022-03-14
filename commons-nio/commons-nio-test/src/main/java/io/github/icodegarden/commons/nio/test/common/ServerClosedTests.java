package io.github.icodegarden.commons.nio.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.exception.remote.TimeoutRemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ServerClosedTests extends CSBaseTests {

	/**
	 * 服务端主动close，客户端send发送消息不会报错,request报响应超时；服务端重启，客户端（无感）自动重试建立连接（by ReconnectTimerTask），恢复正常通讯
	 */
	@Test
	public void serverClose() throws Exception {
		startServer();
		startClient();

		assertEquals(false, nioClient.isClosed());
		assertEquals(false, nioServer.isClosed());

		nioServer.close();//关闭server

		Thread.sleep(500);// wait for

		assertEquals(false, nioClient.isClosed());
		assertEquals(true, nioServer.isClosed());

		nioClient.send(System.currentTimeMillis() + "");// 可以发出
		assertThrows(TimeoutRemoteException.class, () -> {
			nioClient.request("xff2");
		});// 异步发送，但不能响应，报超时 timeout

		new Thread() {
			public void run() {
				try {
					nioServer.start();
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
			};
		}.start();

		Thread.sleep(6000);// wait for server start,client restart

		String response = nioClient.request("xff2");// 恢复正常
		assertEquals("response-xff2", response);
	}

}
