package io.github.icodegarden.commons.nio.netty;

import java.nio.ByteBuffer;

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
class MessageEncoder extends MessageToByteEncoder {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		ByteBuffer byteBuffer = Codec.encode((ExchangeMessage) msg);

		byteBuffer.flip();// 需要flip才能给out读
		out.writeBytes(byteBuffer);
	}
}