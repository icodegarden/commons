package io.github.icodegarden.commons.nio.netty;

import java.net.InetSocketAddress;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.netty.NettyNioClient;
import io.github.icodegarden.commons.nio.netty.NettyNioServer;
import io.github.icodegarden.commons.nio.test.common.ServerClosedTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NettyServerClosedTests extends ServerClosedTests {

	@Override
	protected NioServer nioServer() {
		return new NettyNioServer("netty", new InetSocketAddress("127.0.0.1", 8888), 3000, messageHandler);//时间配置3000
	}

	@Override
	protected NioClient nioClient() {
		return new NettyNioClient(new InetSocketAddress("127.0.0.1", 8888), 3000);//时间配置3000
	}
}
