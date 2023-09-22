package io.github.icodegarden.commons.exchange.nio;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.util.ClassUtils;

import io.github.icodegarden.commons.exchange.ParallelExchangeResult;
import io.github.icodegarden.commons.exchange.ParallelExchanger;
import io.github.icodegarden.commons.exchange.ParallelLoadBalanceExchanger;
import io.github.icodegarden.commons.exchange.ReasonExchangeResult;
import io.github.icodegarden.commons.exchange.broadcast.Broadcast;
import io.github.icodegarden.commons.exchange.broadcast.BroadcastMessage;
import io.github.icodegarden.commons.exchange.exception.ExchangeException;
import io.github.icodegarden.commons.exchange.exception.NoQualifiedInstanceExchangeException;
import io.github.icodegarden.commons.exchange.loadbalance.AllInstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.EmptyInstanceLoadBalance;
import io.github.icodegarden.commons.exchange.loadbalance.InstanceLoadBalance;
import io.github.icodegarden.commons.lang.Matcher;
import io.github.icodegarden.commons.lang.concurrent.registry.Instance;
import io.github.icodegarden.commons.lang.util.LogUtils;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.MessageHandlerProvider;
import io.github.icodegarden.commons.nio.NioServer;
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

	private AtomicBoolean serverStarted = new AtomicBoolean();
	/**
	 * 是否也对本地
	 */
	private boolean broadcastLocal = false;

	private final Supplier<List<Instance>> instancesSupplier;

	private final String host;/* 对外网络ip */
	private final int serverPort;
	private final EntryMessageHandler entryMessageHandler;

	private final ParallelLoadBalanceExchanger parallelLoadBalanceExchanger;

	private NioClientPool nioClientPool;
	private NioServer nioServer;

	public NioBroadcast(String host, int serverPort, Supplier<List<Instance>> instancesSupplier,
			MessageHandler<BroadcastMessage, ReasonExchangeResult> serverMessageHandler) {
		this.instancesSupplier = instancesSupplier;

		// ----------------------------------------------------------------------
		nioClientPool = NioClientPool.newPool("NioBroadcast", NioClientSuppliers.DEFAULT);
		NioProtocol protocol = new NioProtocol(nioClientPool);

		ParallelExchanger.Config config = new ParallelExchanger.Config(1, Integer.MAX_VALUE, Integer.MAX_VALUE);
		parallelLoadBalanceExchanger = new ParallelLoadBalanceExchanger(protocol, new EmptyInstanceLoadBalance(),
				null/* 无感 */, config);

		// ----------------------------------------------------------------------
		this.host = host;
		this.serverPort = serverPort;
		this.entryMessageHandler = new EntryMessageHandler((MessageHandler) serverMessageHandler);
	}

	public void addMessageHandlerProvider(MessageHandlerProvider<BroadcastMessage, ReasonExchangeResult> provider) {
		entryMessageHandler.addMessageHandlerProvider((MessageHandlerProvider) provider);
	}

	public void startServer() {
		if (serverStarted.compareAndSet(false, true)) {
			InetSocketAddress bind = new InetSocketAddress(host, this.serverPort);

			if (ClassUtils.isPresent("io.github.icodegarden.commons.nio.netty.NettyNioServer", null)) {
				try {
					Class<?> cla = ClassUtils.forName("io.github.icodegarden.commons.nio.netty.NettyNioServer", null);
					Constructor<?> constructor = cla.getDeclaredConstructor(String.class, InetSocketAddress.class,
							MessageHandler.class);
					this.nioServer = (NioServer) constructor.newInstance("NioBroadcast-Server", bind,
							entryMessageHandler);
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			} else {
				this.nioServer = new JavaNioServer("NioBroadcast-Server", bind, entryMessageHandler);
			}

			try {
				this.nioServer.start();
			} catch (IOException e) {
				serverStarted.set(false);
				throw new IllegalStateException("error on start Nio Server.", e);
			}
		}
	}

	public void setBroadcastLocal(boolean broadcastLocal) {
		this.broadcastLocal = broadcastLocal;
	}

	private boolean isLocal(String address, int port) {
		return host.equals(address) && port == serverPort;
	}

	@Override
	public ParallelExchangeResult request(BroadcastMessage message) throws ExchangeException {
		Matcher<Instance> matcher = new io.github.icodegarden.commons.lang.Matcher<Instance>() {
			@Override
			public boolean matches(Instance instance) {
				if (!broadcastLocal && isLocal(instance.getHost(), instance.getPort())) {
					/**
					 * 如果不需要对本地
					 */
					return false;
				}

				if (message.instanceMatcher() == null) {
					return true;
				}

				return message.instanceMatcher().matches(instance);
			}
		};

		InstanceLoadBalance instanceLoadBalance = new AllInstanceLoadBalance(
				new BroadCastInstanceDiscovery(instancesSupplier, matcher));
		try {
			return parallelLoadBalanceExchanger.exchange(message, (int) message.timeoutMillis(), instanceLoadBalance);
		} catch (NoQualifiedInstanceExchangeException e) {
			/**
			 * 如果没有其他实例而只有本实例，则会进这里
			 */
			LogUtils.infoIfEnabled(log, () -> log.info("No Any Instance Should Broadcast."));
			return new ParallelExchangeResult(Collections.emptyList());
		}
	}

	@Override
	public void close() throws IOException {
		/**
		 * 先关闭客户端
		 */
		if (nioClientPool != null) {
			nioClientPool.close();
		}

		if (nioServer != null) {
			nioServer.close();
		}
	}

}
