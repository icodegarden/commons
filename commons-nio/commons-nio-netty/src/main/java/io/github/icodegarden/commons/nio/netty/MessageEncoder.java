package io.github.icodegarden.commons.nio.netty;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Codec;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class MessageEncoder extends MessageToByteEncoder<Object> {
	private static final Logger log = LoggerFactory.getLogger(MessageEncoder.class);

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("encode nio msg from:{}", ctx.channel());
		}
		try{
			ByteBuffer byteBuffer = Codec.encode((ExchangeMessage) msg);
			byteBuffer.flip();// 需要flip才能给out读
			out.writeBytes(byteBuffer);
		}catch (Throwable e) {
			log.error("ex on MessageEncoder", e);
			throw e;
		}
	}
	
}