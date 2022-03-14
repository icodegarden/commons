package io.github.icodegarden.commons.nio.java;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.ClientClosedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ConnectFailedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.nio.AbstractNioClient;
import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.health.Heartbeat;
import io.github.icodegarden.commons.nio.health.NioClientHeartbeat;
import io.github.icodegarden.commons.nio.task.HeartbeatTimerTask;
import io.github.icodegarden.commons.nio.task.ReconnectTimerTask;
import io.github.icodegarden.commons.nio.task.ScheduleCancelableRunnable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaNioClient extends AbstractNioClient implements ClientNioEventListener {
	private static final Logger log = LoggerFactory.getLogger(JavaNioClient.class);

	private SocketChannelSpace socketChannels;

	private final ClientNioSelector clientNioSelector;
	private final HeartbeatTimerTask heartbeatTimerTask;
	private final ReconnectTimerTask reconnectTimerTask;

	private volatile boolean closed = true;

	private SocketChannel socketChannel;

	private Heartbeat heartbeat;

	private final InetSocketAddress address;

	private ScheduleCancelableRunnable heartbeatTask;
	private ScheduleCancelableRunnable reconnectTask;
	/**
	 * 被动关闭，非用户行为
	 */
	private Runnable clientPassiveCloseListener;

	public JavaNioClient(InetSocketAddress address, ClientNioSelector clientNioSelector) {
		this(address, clientNioSelector, HeartbeatTimerTask.DEFAULT, ReconnectTimerTask.DEFAULT);
	}

	public JavaNioClient(InetSocketAddress address, ClientNioSelector clientNioSelector, long heartbeatIntervalMillis) {
		this(address, clientNioSelector, new HeartbeatTimerTask(heartbeatIntervalMillis),
				new ReconnectTimerTask(heartbeatIntervalMillis));
	}

	JavaNioClient(InetSocketAddress address, ClientNioSelector clientNioSelector, HeartbeatTimerTask heartbeatTimerTask,
			ReconnectTimerTask reconnectTimerTask) {
		this.address = address;
		this.clientNioSelector = clientNioSelector;
		this.heartbeatTimerTask = heartbeatTimerTask;
		this.reconnectTimerTask = reconnectTimerTask;
	}

	/**
	 * 被动关闭时
	 * 
	 * @param listener
	 */
	public void setClientPassiveCloseListener(Runnable listener) {
		this.clientPassiveCloseListener = listener;
	}

	@Override
	public synchronized void connect() throws ConnectFailedRemoteException {
		try {
			socketChannel = SocketChannel.open();

			socketChannel.configureBlocking(true);
			socketChannel.socket().setSoLinger(false, -1);
			socketChannel.socket().setTcpNoDelay(true);
			socketChannel.socket().setReceiveBufferSize(1024 * 64);
			socketChannel.socket().setSendBufferSize(1024 * 64);

			socketChannel.configureBlocking(false);

			if (log.isInfoEnabled()) {
				log.info("client connecting...");
			}

			socketChannel.connect(address);// non blocking模式结果一定是false

			int retry = 3;
			while (!finishConnect()) {
				if (++retry == 3) {
					throw new ConnectFailedRemoteException("NOT finishConnect, retry:" + retry);
				}
			}
			if (log.isInfoEnabled()) {
				log.info("client connected");
			}

			socketChannels = new SocketChannelSpace("client");

			closed = false;
		} catch (IOException e) {
			throw new ConnectFailedRemoteException("Connect Failed", e);
		}
	}

	private boolean finishConnect() throws IOException, ClosedChannelException {
		boolean connected = socketChannel.isConnected();
		if (connected || (connected = socketChannel.finishConnect())) {
			if (log.isInfoEnabled()) {
				log.info("client finishConnect");
			}

			clientNioSelector.registerRead(this);

			if (heartbeat == null) {// IMPT 这些不能（也不需要）重新创建，否则task pool没有清除
				heartbeat = new NioClientHeartbeat("client", this, new Channel() {
					@Override
					public void write(Object obj) throws RemoteException {
						try{
							socketChannels.write(socketChannel(), (ExchangeMessage) obj);
						} catch (Exception e) {
							throw new ExceedExpectedRemoteException(e);
						}
					}

					@Override
					public void close() throws IOException {
						socketChannel.close();
					}
				});
				heartbeatTask = heartbeatTimerTask.register(heartbeat);
				reconnectTask = reconnectTimerTask.register(heartbeat);
			}
		}
		return connected;
	}

	@Override
	public SocketChannel socketChannel() {
		return socketChannel;
	}

	@Override
	public void onRead(SelectionKey key) throws IOException {
		try {
			ExchangeMessage message = socketChannels.read(socketChannel);
			if (message == null) {
				return;// wait for more read
			}

			if (message.isEvent()) {
				heartbeat.receive();
			} else {
				if (log.isDebugEnabled()) {
					log.debug("client read message:{}", message);
				}
				Future.received(message.getRequestId(), message.getBody());
			}
		} catch (ClosedChannelException e) {
			// 通常是client自身网络断开
			if (log.isWarnEnabled()) {
				log.warn("client channel was closed, reconnect...");
			}
			reconnect();
		} catch (IOException e) {
			// 通常由于server已经关闭
			if (log.isWarnEnabled()) {
				log.warn("client channel was closed, that more means server was closed, close...");
			}
			closeInternal();
		}
	}

	@Override
	public void onWrite(SelectionKey key) throws IOException {
		throw new IOException("unuse");
	}

	@Override
	protected void doSend(ExchangeMessage message) throws RemoteException {
		if (log.isDebugEnabled()) {
			log.debug("client send message:{}", message);
		}
//		不做判断与重连，让heartbeat处理重连、监听close事件处理连接池
//		try {
//			if(isClosed()) {
//				reconnect();
//			}
//		} catch (IOException e) {
//			throw new NioException(e);
//		}
		
		/**
		 * nio不需要关注ServerError，
		 */
		try {
			socketChannels.write(socketChannel, message);
		} catch (IOException e) {
			/**
			 * 客户端已人工关闭<br>
			 * 网络故障等，让heartbeat处理重连、监听close事件处理连接池
			 */
			if (isClosed()) {
				throw new ClientClosedRemoteException("client closed", e);
			}
			throw new ExceedExpectedRemoteException(e);
		}
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

	@Override
	public synchronized void reconnect() throws ConnectFailedRemoteException {
		if (log.isInfoEnabled()) {
			log.info("client do reconnect...");
		}
		// 由于heartbeatTask reconnectTask 不能关闭，不能调用close
		try {
			socketChannel.close();
		} catch (IOException e) {
			throw new ConnectFailedRemoteException("Connect Failed", e);
		}
		closed = true;
		connect();
	}

	@Override
	public synchronized void close() throws IOException {
		/**
		 * 用户主动发起
		 */
		try {
			if (socketChannel != null) {
				socketChannel.close();
			}
			if (heartbeatTask != null) {
				heartbeatTask.cancel();
			}
			if (reconnectTask != null) {
				reconnectTask.cancel();
			}
		} finally {
			closed = true;
		}
	}

	private synchronized void closeInternal() throws IOException {
		close();
		if (clientPassiveCloseListener != null) {
			clientPassiveCloseListener.run();
		}
	}

	@Override
	public String toString() {
		return "[closed=" + closed + ", socketChannel=" + socketChannel + "]";
	}
}
