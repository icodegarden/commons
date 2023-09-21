package io.github.icodegarden.commons.exchange.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Supplier;

import io.github.icodegarden.commons.exchange.ParallelExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelExchanger;
import io.github.icodegarden.commons.exchange.ParallelLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.broadcast.Broadcast;
import io.github.icodegarden.commons.exchange.broadcast.BroadcastMessage;
import io.github.icodegarden.commons.exchange.exception.ExchangeFailedReason;
import io.github.icodegarden.commons.exchange.loadbalance.AllInstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.EmptyInstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.lang.Matcher;
import io.github.icodegarden.commons.lang.concurrent.registry.Instance;
import io.github.icodegarden.commons.lang.result.Result2;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.java.JavaNioServer;
import io.github.icodegarden.commons.nio.pool.NioClientPool;
import io.github.icodegarden.commons.nio.pool.NioClientSuppliers;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class NioBroadcast implements Broadcast {

	/**
	 * 是否也对本地
	 */
	private boolean broadcastLocal = false;

	private final Supplier<List<Instance>> instancesSupplier;

	private final String host;/* 对外网络ip */
	private final int serverPort;
	private final MessageHandler<BroadcastMessage, Result2<Object, ExchangeFailedReason>> serverMessageHandler;

	private final ParallelLoadBalanceExchanger parallelLoadBalanceExchanger;

	private NioClientPool nioClientPool;
	private JavaNioServer javaNioServer;

	public NioBroadcast(String host, int serverPort, Supplier<List<Instance>> instancesSupplier,
			MessageHandler<BroadcastMessage, Result2<Object, ExchangeFailedReason>> serverMessageHandler) {
		this.instancesSupplier = instancesSupplier;
		this.serverMessageHandler = serverMessageHandler;

		// ----------------------------------------------------------------------
		nioClientPool = NioClientPool.newPool("NioBroadcast", NioClientSuppliers.DEFAULT);
		NioProtocol protocol = new NioProtocol(nioClientPool);

		ParallelExchanger.Config config = new ParallelExchanger.Config(1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		parallelLoadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, new EmptyInstanceLoadBalance(),
				null/* 无感 */, config);

		// ----------------------------------------------------------------------
		this.host = host;
		this.serverPort = serverPort;
	}

	public void startServer() {
		InetSocketAddress bind = new InetSocketAddress(host, this.serverPort);

		this.javaNioServer = new JavaNioServer("NioBroadcast-Server", bind,
				new EntryMessageHandler((MessageHandler) serverMessageHandler));

		try {
			javaNioServer.start();
		} catch (IOException e) {
			throw new IllegalStateException("error on start Nio Server.", e);
		}
	}

	public void setBroadcastLocal(boolean broadcastLocal) {
		this.broadcastLocal = broadcastLocal;
	}

	private boolean isLocal(String address, int port) {
		return host.equals(address) && port == serverPort;
	}
	
	@Override
	public ParallelExchangeResult request(BroadcastMessage message) {
		Matcher<Instance> matcher = new io.github.icodegarden.commons.lang.Matcher<Instance>() {
			@Override
			public boolean matches(Instance instance) {
				if (!broadcastLocal && isLocal(instance.getHost(), instance.getPort())) {
					/**
					 * 如果不需要对本地
					 */
					return false;
				}
				
				if(message.instanceMatcher() == null) {
					return true;
				}

				return message.instanceMatcher().matches(instance);
			}
		};

		InstanceLoadBalance instanceLoadBalance = new AllInstanceLoadBalance(
				new BroadCastInstanceDiscovery(instancesSupplier, matcher));
		return parallelLoadBalanceExchanger.exchange(message, (int) message.timeoutMillis(), instanceLoadBalance);
	}

	@Override
	public void close() throws IOException {
		/**
		 * 先关闭客户端
		 */
		if (nioClientPool != null) {
			nioClientPool.close();
		}

		if (javaNioServer != null) {
			javaNioServer.close();
		}
	}

}
