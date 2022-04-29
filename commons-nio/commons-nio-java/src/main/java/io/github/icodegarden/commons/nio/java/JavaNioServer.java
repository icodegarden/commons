package io.github.icodegarden.commons.nio.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.concurrent.NamedThreadFactory;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.NioServer;
import io.github.icodegarden.commons.nio.task.IdleStateTimerTask;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaNioServer implements NioServer, ServerNioEventListener {
	private static final Logger log = LoggerFactory.getLogger(JavaNioServer.class);

	private ThreadPoolExecutor workerThreadPool = new ThreadPoolExecutor(20, 200, 120, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(100), new NamedThreadFactory("Nio-ServerSide-MessageHandlerStrategy"),
			new ThreadPoolExecutor.CallerRunsPolicy());

	private final String name;
	private final InetSocketAddress bind;
	private final MessageHandler messageHandler;

	private volatile boolean closed = true;
	private ServerSocketChannel serverSocketChannel;
	private ServerNioSelector nioServerSelector;
	private IdleStateTimerTask idleStateTimerTask;

	public JavaNioServer(String name, InetSocketAddress bind, MessageHandler messageHandler) {
		this(name, bind, IdleStateTimerTask.DEFAULT, messageHandler);
	}

	public JavaNioServer(String name, InetSocketAddress bind, long clientHeartbeatIntervalMillis,
			MessageHandler messageHandler) {
		this(name, bind, new IdleStateTimerTask(clientHeartbeatIntervalMillis), messageHandler);
	}

	private JavaNioServer(String name, InetSocketAddress bind, IdleStateTimerTask idleStateTimerTask,
			MessageHandler messageHandler) {
		this.name = name;
		this.bind = bind;
		this.idleStateTimerTask = idleStateTimerTask;
		this.messageHandler = messageHandler;
	}

	public void setWorkerThreadPool(ThreadPoolExecutor threadpool) {
		this.workerThreadPool = threadpool;
	}

	public void start() throws IOException {
		if (log.isInfoEnabled()) {
			log.info("starting nio server...");
		}

		nioServerSelector = ServerNioSelector.openNew(name);

		serverSocketChannel = ServerSocketChannel.open();
		serverSocketChannel.bind(bind);
		serverSocketChannel.configureBlocking(false);

		nioServerSelector.registerAccept(this);
		nioServerSelector.start();

		if (log.isInfoEnabled()) {
			log.info("started nio server:{}", bind);
		}
		closed = false;
	}

	@Override
	public ServerSocketChannel serverSocketChannel() {
		return serverSocketChannel;
	}

	@Override
	public void onAccept() throws IOException {
		SocketChannel socketChannel = serverSocketChannel.accept();
		if (log.isInfoEnabled()) {
			log.info("accept a new socket, address:{}", socketChannel);
		}
		socketChannel.configureBlocking(false);

		ServerSideClient serverSideClient = new ServerSideClient(workerThreadPool, socketChannel, idleStateTimerTask,
				messageHandler);

		nioServerSelector.registerRead(serverSideClient);
		if (log.isInfoEnabled()) {
			log.info("register socket, address:{}", socketChannel);
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public void close() throws IOException {
		if (log.isInfoEnabled()) {
			log.info("start server close...");
		}
		nioServerSelector.close();
		serverSocketChannel.close();
		workerThreadPool.shutdown();
		closed = true;
		if (log.isInfoEnabled()) {
			log.info("server closed");
		}

	}
}
