package io.github.icodegarden.commons.nio.netty;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class NettyNioServer implements NioServer {

	private int nettyWorkerThreads = 200;
	
	private volatile boolean closed = true;
	private ServerBootstrap bootstrap;

	private Channel channel;

	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	private final String name;
	private final InetSocketAddress bind;
	private long clientHeartbeatIntervalMillis;
	private final MessageHandler messageHandler;

	public NettyNioServer(String name, InetSocketAddress bind, MessageHandler messageHandler) {
		this(name, bind, HeartbeatTimerTask.DEFAULT_INTERVAL_MILLIS, messageHandler);
	}

	NettyNioServer(String name, InetSocketAddress bind, long clientHeartbeatIntervalMillis,
			MessageHandler messageHandler) {
		this.name = name;
		this.bind = bind;
		this.clientHeartbeatIntervalMillis = clientHeartbeatIntervalMillis;
		this.messageHandler = messageHandler;
	}

	public void setNettyWorkerThreads(int nettyWorkerThreads) {
		this.nettyWorkerThreads = nettyWorkerThreads;
	}
	
	public void setClientHeartbeatIntervalMillis(long clientHeartbeatIntervalMillis) {
		this.clientHeartbeatIntervalMillis = clientHeartbeatIntervalMillis;
	}
	
	@Override
	public void start() throws IOException {
		doOpen();
	}

	@Override
	public void close() throws IOException {
		doClose();
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	private void doOpen() {
		bootstrap = new ServerBootstrap();
		bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory(name + "-NettyServerBoss", true));
		workerGroup = new NioEventLoopGroup(nettyWorkerThreads, new DefaultThreadFactory(name + "-NettyServerWorker", true));

		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
				.childOption(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
				.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
				.childHandler(new ChannelInitializer<NioSocketChannel>() {
					@Override
					protected void initChannel(NioSocketChannel ch) throws Exception {
						final ServerHandler nettyServerHandler = new ServerHandler(messageHandler);

						ch.pipeline().addLast(new MessageDecoder()).addLast(new MessageEncoder())
								.addLast("server-idle-handler", new IdleStateHandler(0, 0,
										clientHeartbeatIntervalMillis * 3, TimeUnit.MILLISECONDS))
								.addLast("handler", nettyServerHandler);
					}
				});
		// bind
		ChannelFuture channelFuture = bootstrap.bind(bind);
		channelFuture.syncUninterruptibly();
		channel = channelFuture.channel();

		closed = false;
	}

	private void doClose() {
		try {
			if (channel != null) {
				channel.close();
			}
		} finally {
			if (bootstrap != null) {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
			closed = true;
		}
	}

}
