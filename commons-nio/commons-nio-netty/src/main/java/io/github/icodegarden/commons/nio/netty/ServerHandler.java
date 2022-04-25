package io.github.icodegarden.commons.nio.netty;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.nio.Channel;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.MessageHandler;
import io.github.icodegarden.commons.nio.concurrent.MessageHandlerStrategy;
import io.github.icodegarden.commons.nio.concurrent.SyncMessageHandlerStrategy;
import io.github.icodegarden.commons.nio.health.Heartbeat;
import io.github.icodegarden.commons.nio.health.ServerSideClientHeartbeat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@io.netty.channel.ChannelHandler.Sharable
class ServerHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);

	private final MessageHandler messageHandler;
	private MessageHandlerStrategy messageHandlerStrategy;

	public ServerHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("client channel:{} active", ctx.channel());
		}
		Channel channel = new Channel() {
			@Override
			public void write(Object obj) throws RemoteException {
				try{
					ctx.writeAndFlush(obj);
				} catch (Exception e) {
					throw new ExceedExpectedRemoteException(e);
				}
			}

			@Override
			public void close() throws IOException {
				ctx.close();
			}
		};
		Heartbeat heartbeat = new ServerSideClientHeartbeat("netty-server-client", channel);
		/**
		 * 使用相同的策略，netty比java原生方式慢大约15%
		 */
		messageHandlerStrategy = new SyncMessageHandlerStrategy(heartbeat, messageHandler, channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (log.isInfoEnabled()) {
			log.info("client channel:{} inactive", ctx.channel());
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		messageHandlerStrategy.handle((ExchangeMessage) msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			if (log.isInfoEnabled()) {
				log.info("client channel:{} was idle, close it", ctx.channel());
			}
			ctx.close();
		}
		super.userEventTriggered(ctx, evt);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("ex of server side client channel:{}", ctx.channel(), cause);
	}

}