package io.github.icodegarden.commons.nio.java;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.test.common.ServerClosedTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaServerClosedTests extends ServerClosedTests {

	JavaCS cs = new JavaCS();

	@Override
	protected NioServer nioServer() {
		return cs.nioServer();//时间配置3000
	}

	@Override
	protected NioClient nioClient() {
		return cs.nioClient();//时间配置3000
	}

}
