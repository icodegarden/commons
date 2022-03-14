package io.github.icodegarden.commons.nio.java;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.concurrent.MessageHandlerStrategy;
import io.github.icodegarden.commons.nio.concurrent.ThreadPoolMessageHandlerStrategy;
import io.github.icodegarden.commons.nio.health.Heartbeat;
import io.github.icodegarden.commons.nio.health.ServerSideClientHeartbeat;
import io.github.icodegarden.commons.nio.task.IdleStateTimerTask;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ServerSideClient implements ClientNioEventListener, Closeable {
	private static Logger log = LoggerFactory.getLogger(ServerSideClient.class);

	private final SocketChannelSpace socketChannels = new SocketChannelSpace("server-client");
	private final SocketChannel socketChannel;
	private final Heartbeat heartbeat;

	private final MessageHandlerStrategy messageHandlerStrategy;
	private final Channel channel;

	public ServerSideClient(SocketChannel socketChannel, IdleStateTimerTask idleStateTimerTask,
			MessageHandler messageHandler) {
		this.socketChannel = socketChannel;
		
		channel = new Channel() {
			@Override
			public void write(Object obj) throws RemoteException {
				try{
					socketChannels.write(socketChannel, (ExchangeMessage)obj);
				} catch (Exception e) {
					throw new ExceedExpectedRemoteException(e);
				}
			}
			@Override
			public void close() throws IOException {
				socketChannel.close();
			}
		};
		
		heartbeat = new ServerSideClientHeartbeat("server", channel);
		idleStateTimerTask.register(heartbeat);
		
		messageHandlerStrategy = new ThreadPoolMessageHandlerStrategy(heartbeat, messageHandler,channel);
	}

	@Override
	public SocketChannel socketChannel() {
		return socketChannel;
	}

	@Override
	public void onRead(SelectionKey key) throws IOException {
		SocketChannel channel = (SocketChannel) key.channel();
		try {
			ExchangeMessage message = socketChannels.read(channel);
			if (message == null) {
				return;// wait for more read
			}
			if (log.isDebugEnabled()) {
				log.debug("server read message:{}", message);
			}

			messageHandlerStrategy.handle(message);

//			key.interestOps(SelectionKey.OP_WRITE);onWrite not use
		} catch (ClosedChannelException e) {
			if (log.isWarnEnabled()) {
				log.warn("server side client was closed by client side, channel:{}", channel);
			}
			channel.close();// 如果客户端已经关闭，读取时就会异常，因此close
		}
	}

	@Override
	public void onWrite(SelectionKey key) throws IOException {
		throw new IOException("unuse");
//		if(response.isEvent()) {
//			heartbeat.send();
//		}else {
//			SocketChannel channel = (SocketChannel) key.channel();
//			System.out.println("server写start，address:"+channel);
//			ByteBuffer buffer = codec.encode(response);
//			try {
//				buffer.flip();//必须，flip后才能读
//				while(buffer.hasRemaining()) {
//					System.out.println("server sending... "+response.getBody());
//					channel.write(buffer);
//				}
//			} catch (IOException e) {
//				throw new RuntimeException(e);
//			}
//			System.out.println("server写end");
//		}
//		key.interestOps(SelectionKey.OP_READ);//写完就改成读
	}

	@Override
	public void close() throws IOException {
		heartbeat.close();
		socketChannel.close();
	}

}
