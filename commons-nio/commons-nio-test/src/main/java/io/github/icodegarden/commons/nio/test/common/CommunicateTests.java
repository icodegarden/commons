package io.github.icodegarden.commons.nio.test.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class CommunicateTests extends CSBaseTests {

	/**
	 * 测试建立连接后，正常通讯
	 */
	@Test
	public void communicate() throws Exception {
		startServer();
		startClient();

		nioClient.send("xff666");
		nioClient.send("xff777");
		nioClient.send("xff888");

		String req = UUID.randomUUID().toString();

		String response = nioClient.request(req);
		assertEquals("response-" + req, response);

		response = nioClient.request(50.05);
		assertEquals("response-50.05", response);

		response = nioClient.request(100);
		assertEquals("response-100", response);

		byte[] _10MB = new byte[1024 * 1024 * 10];// 10M
		response = nioClient.request(_10MB);
	}

}
