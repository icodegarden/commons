package io.github.icodegarden.commons.nio.java;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.test.common.CommunicateTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaCommunicateTests extends CommunicateTests {

	JavaCS cs = new JavaCS();

	@Override
	protected NioServer nioServer() {
		return cs.nioServer();
	}

	@Override
	protected NioClient nioClient() {
		return cs.nioClient();
	}

}
