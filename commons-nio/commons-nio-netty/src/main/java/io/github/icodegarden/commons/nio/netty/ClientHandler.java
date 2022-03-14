package io.github.icodegarden.commons.nio.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.github.icodegarden.commons.nio.AbstractNioClient.Future;
import io.github.icodegarden.commons.nio.health.NioClientHeartbeat;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@io.netty.channel.ChannelHandler.Sharable
class ClientHandler extends ChannelDuplexHandler {
	private static final Logger log = LoggerFactory.getLogger(NettyNioClient.class);

	private final NioClientHeartbeat heartbeat;
	
	public ClientHandler(NioClientHeartbeat heartbeat) {
		this.heartbeat = heartbeat;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ExchangeMessage message = (ExchangeMessage) msg;
		if (message.isEvent()) {
			heartbeat.receive();
		} else {
			if (log.isDebugEnabled()) {
				log.debug("client read message:{}", message);
			}
			/**
			 * netty收到任何消息时也视为收到了心跳。如果不这样做，在持续有消息收到的情况下，
			 * 客户端的心跳事件userEventTriggered不会触发，因此也不会有自服务端的心跳更新，ReconnectTimerTask会误判
			 */
			heartbeat.receive();
			
			Future.received(message.getRequestId(), message.getBody());
		}
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write(ctx, msg, promise);
		/**
		 * 
		 */
//		heartbeat.refreshLastSend();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			heartbeat.send();
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("ex of client channel [{}]", ctx.channel(), cause);
	}
}
