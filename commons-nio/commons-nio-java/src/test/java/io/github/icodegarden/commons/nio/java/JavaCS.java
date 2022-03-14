package io.github.icodegarden.commons.nio.java;

import java.net.InetSocketAddress;

import io.github.icodegarden.commons.nio.NioClient;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.java.ClientNioSelector;
import io.github.icodegarden.commons.nio.java.JavaNioClient;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.github.icodegarden.commons.nio.task.ReconnectTimerTask;
import io.github.icodegarden.commons.nio.test.common.CSBaseTests;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaCS extends CSBaseTests {

	ClientNioSelector clientNioSelector = ClientNioSelector.openNew("1");

	HeartbeatTimerTask heartbeatTimerTask = new HeartbeatTimerTask(3000);//时间配置3000
	ReconnectTimerTask reconnectTimerTask = new ReconnectTimerTask(3000);//时间配置3000

	@Override
	protected NioServer nioServer() {
		return new JavaNioServer("s1", new InetSocketAddress("127.0.0.1", 8888), 3000, messageHandler);//时间配置3000
	}

	@Override
	protected NioClient nioClient() {
		return new JavaNioClient(new InetSocketAddress("127.0.0.1", 8888), clientNioSelector, heartbeatTimerTask,//时间配置3000
				reconnectTimerTask);
	}
}
