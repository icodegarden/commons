package io.github.icodegarden.commons.nio.pool;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

import org.springframework.util.ClassUtils;

import io.github.icodegarden.commons.nio.NioClient;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class NioClientSuppliers {

	public static final NioClientSupplier DEFAULT = (ip, port) -> {
		try {
			if (ClassUtils.isPresent("com.github.icodegarden.commons.nio.netty.NettyNioClient", null)) {
				Class<?> cla = ClassUtils.forName("com.github.icodegarden.commons.nio.netty.NettyNioClient", null);
				Constructor<?> constructor = cla.getDeclaredConstructor(InetSocketAddress.class);
				return (NioClient) constructor.newInstance(new InetSocketAddress(ip, port));
			} else {
				Class<?> cla = ClassUtils.forName("io.github.icodegarden.commons.nio.java.JavaNioClient", null);
				Constructor<?> constructor = cla.getDeclaredConstructor(InetSocketAddress.class);
				return (NioClient) constructor.newInstance(new InetSocketAddress(ip, port));
			}
		} catch (Throwable t) {
			throw new RuntimeException(t);
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