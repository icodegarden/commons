package io.github.icodegarden.commons.nio.java;

import java.net.InetSocketAddress;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.java.ClientNioSelector;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.github.icodegarden.commons.nio.task.ReconnectTimerTask;
import io.github.icodegarden.commons.nio.test.common.MultiClientServerSerialTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaMultiClientServerSerialTests extends MultiClientServerSerialTests {

	ClientNioSelector clientNioSelector = ClientNioSelector.openNew("1");
	
	HeartbeatTimerTask heartbeatTimerTask = new HeartbeatTimerTask(3000);
	ReconnectTimerTask reconnectTimerTask = new ReconnectTimerTask(3000);

	@Override
	protected NioServer nioServer1(int port) {
		return new JavaNioServer("s1", new InetSocketAddress("127.0.0.1", port), 3000, messageHandler);
	}

	@Override
	protected NioServer nioServer2(int port) {
		return new JavaNioServer("s2", new InetSocketAddress("127.0.0.1", port), 3000, messageHandler);
	}

	@Override
	protected NioClient nioClient(int port) {
		return new JavaNioClient(new InetSocketAddress("127.0.0.1", port), clientNioSelector, heartbeatTimerTask,
				reconnectTimerTask);
	}

}
