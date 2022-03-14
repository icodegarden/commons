package io.github.icodegarden.commons.exchange;

import java.net.InetSocketAddress;

import io.github.icodegarden.commons.nio.netty.NettyNioClient;

/**
 * 目的是为了NettyNioClient jar包不存在时可用
 * @author Fangfang.Xu
 *
 */
abstract class NettyNioClients {

	public static NettyNioClient create(String ip, int port) {
		return new NettyNioClient(new InetSocketAddress(ip, port));
	}

}