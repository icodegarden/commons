package io.github.icodegarden.commons.nio.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.exception.remote.ClientClosedRemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ClientClosedTests extends CSBaseTests {

	/**
	 * 客户端主动close，服务端不会关闭；客户端发送消息时出异常
	 */
	@Test
	public void clientClose() throws Exception {
		startServer();
		startClient();

		assertEquals(false, nioClient.isClosed());
		assertEquals(false, nioServer.isClosed());

		nioClient.close();

		Thread.sleep(500);// wait for

		assertEquals(true, nioClient.isClosed());
		assertEquals(false, nioServer.isClosed());

		// 已关闭
		assertThrows(ClientClosedRemoteException.class, () -> {
			nioClient.request("xff");
		});// cause ClosedChannelException.class
	}

}
