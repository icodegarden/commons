package io.github.icodegarden.commons.nio.netty;

import java.net.InetSocketAddress;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.netty.NettyNioClient;
import io.github.icodegarden.commons.nio.netty.NettyNioServer;
import io.github.icodegarden.commons.nio.test.common.CommunicateTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NettyCommunicateTests extends CommunicateTests {

	@Override
	protected NioServer nioServer() {
		return new NettyNioServer("netty", new InetSocketAddress("127.0.0.1", 8888), 3000, messageHandler);
	}

	@Override
	protected NioClient nioClient() {
		return new NettyNioClient(new InetSocketAddress("127.0.0.1", 8888), 3000);
	}
}
