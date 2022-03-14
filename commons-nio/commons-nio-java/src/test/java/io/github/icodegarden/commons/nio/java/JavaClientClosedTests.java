package io.github.icodegarden.commons.nio.java;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.test.common.ClientClosedTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaClientClosedTests extends ClientClosedTests {

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
