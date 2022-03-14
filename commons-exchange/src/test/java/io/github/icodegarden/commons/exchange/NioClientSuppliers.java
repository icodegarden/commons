package io.github.icodegarden.commons.exchange;

import java.net.InetSocketAddress;

import org.springframework.util.ClassUtils;

import io.github.icodegarden.commons.nio.java.ClientNioSelector;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.pool.NioClientSupplier;

/**
 * 
 * @author Fangfang.Xu
 *
 */
abstract class NioClientSuppliers {

	public static final NioClientSupplier DEFAULT = (ip, port) -> {
		if (ClassUtils.isPresent("com.github.icodegarden.commons.nio.netty.NettyNioClient", null)) {
			return NettyNioClients.create(ip, port);
		} else {
			ClientNioSelector clientNioSelector = ClientNioSelector.openNew("commons-exchange");
			return new JavaNioClient(new InetSocketAddress(ip, port), clientNioSelector);
		}
	};
	
//	public static final NioClientSupplier NETTY = (ip, port) -> {
//		return NettyNioClients.create(ip, port);
//	};
//	
//	public static final NioClientSupplier JAVA = (ip, port) -> {
//		ClientNioSelector clientNioSelector = null;
//		try {
//			clientNioSelector = ClientNioSelector.openNew("commons-exchange");
//		} catch (IOException e) {
//			throw new ExceedExpectedNioException(e);
//		}
//		return new JavaNioClient(new InetSocketAddress(ip, port), clientNioSelector);
//	};

}