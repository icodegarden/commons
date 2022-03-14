package io.github.icodegarden.commons.nio.netty;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.ClientClosedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ConnectFailedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.nio.AbstractNioClient;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.health.NioClientHeartbeat;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.github.icodegarden.commons.nio.task.ReconnectTimerTask;
import io.github.icodegarden.commons.nio.task.ScheduleCancelableRunnable;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NettyNioClient extends AbstractNioClient implements io.github.icodegarden.commons.nio.Channel {

	private static final Logger log = LoggerFactory.getLogger(NettyNioClient.class);

	private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(
			Math.min(Runtime.getRuntime().availableProcessors() + 1, 32),
			new DefaultThreadFactory("NettyClientWorker", true));

	private volatile boolean closed = true;

	private Bootstrap bootstrap;

	private volatile Channel channel; // volatile, please copy reference to use

	private final InetSocketAddress address;
	private int connectTimeout = 3000;
	private final long heartbeatIntervalMillis;

	private NioClientHeartbeat heartbeat;
	private final ReconnectTimerTask reconnectTimerTask;
	private ScheduleCancelableRunnable reconnectTask;

	public NettyNioClient(InetSocketAddress address) {
		this(address, HeartbeatTimerTask.DEFAULT_INTERVAL_MILLIS);
	}

	public NettyNioClient(InetSocketAddress address, long heartbeatIntervalMillis) {
		this.address = address;
		this.heartbeatIntervalMillis = heartbeatIntervalMillis;
		this.reconnectTimerTask = new ReconnectTimerTask(heartbeatIntervalMillis);
		doOpen();
	}

	private void doOpen() {
		heartbeat = new NioClientHeartbeat("netty client", this, this);
		ClientHandler nettyClientHandler = new ClientHandler(heartbeat);

		bootstrap = new Bootstrap();
		bootstrap.group(nioEventLoopGroup).option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout).channel(NioSocketChannel.class);

		bootstrap.handler(new ChannelInitializer() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new MessageDecoder()).addLast(new MessageEncoder())
						.addLast("client-idle-handler",
								new IdleStateHandler(heartbeatIntervalMillis, 0, 0, TimeUnit.MILLISECONDS))
						.addLast("handler", nettyClientHandler);
			}
		});
	}

	@Override
	public synchronized void connect() throws ConnectFailedRemoteException {
		try {
			ChannelFuture future = bootstrap.connect(address);
			boolean ret = future.awaitUninterruptibly(3000, MILLISECONDS);

			if (ret && future.isSuccess()) {
				Channel newChannel = future.channel();
				this.channel = newChannel;

				if (reconnectTask == null) {// IMPT 也不需要重新创建
					reconnectTask = reconnectTimerTask.register(heartbeat);
				}

				closed = false;
			} else if (future.cause() != null) {
				throw future.cause();
			} else {
				throw new ConnectFailedRemoteException("connect not success");
			}
		} catch (RemoteException e) {
			throw e;
		} catch (Throwable e) {
			throw new ConnectFailedRemoteException("Connect Failed", e);
		}
	}

	@Override
	public synchronized void reconnect() throws ConnectFailedRemoteException {
		if (log.isInfoEnabled()) {
			log.info("client do reconnect...");
		}
		// 由于reconnectTask 不能关闭，不能调用close
		try {
			if (channel != null) {
				channel.close();
			}
		} catch (Exception e) {
			throw new ConnectFailedRemoteException("Connect Failed", e);
		}

		closed = true;
		connect();
	}

//	private Channel getChannel() {
//		Channel c = channel;
//		if (c == null || !c.isActive()) {
//			return null;
//		}
//		return c;
//	}
//
//	private boolean canHandleIdle() {
//		return true;
//	}

	@Override
	public void write(Object obj) throws RemoteException {
		if (log.isDebugEnabled()) {
			log.debug("client send:{}", obj);
		}
		if (isClosed()) {
			throw new ClientClosedRemoteException("client closed");
		}
		try {
			channel.writeAndFlush(obj);
		} catch (Exception e) {
			throw new ExceedExpectedRemoteException(e);
		}
	}

	@Override
	protected void doSend(ExchangeMessage message) throws RemoteException {
		if (log.isDebugEnabled()) {
			log.debug("client send message:{}", message);
		}
		if (isClosed()) {
			throw new ClientClosedRemoteException("client closed");
		}
		try{
			channel.writeAndFlush(message);
		} catch (Exception e) {
			throw new ExceedExpectedRemoteException(e);
		}
	}

	@Override
	public synchronized void close() throws IOException {
		/**
		 * 用户主动发起
		 */
		try {
			if (channel != null) {
				channel.close();
			}
			if (reconnectTask != null) {
				reconnectTask.cancel();
			}
		} finally {
			closed = true;
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

}