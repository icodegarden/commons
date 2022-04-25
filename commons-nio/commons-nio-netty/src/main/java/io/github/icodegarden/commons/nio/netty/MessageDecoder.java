package io.github.icodegarden.commons.nio.netty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Codec;
import io.github.icodegarden.commons.nio.ExchangeMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
/**
 * 
 * @author Fangfang.Xu
 *
 */
class MessageDecoder extends ByteToMessageDecoder {
	private static final Logger log = LoggerFactory.getLogger(MessageDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf input, List<Object> out) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug("decode nio msg from:{}", ctx.channel());
		}
		do {
			int saveReaderIndex = input.readerIndex();
			ExchangeMessage msg = decode(input);
			if (msg == null) {
				input.readerIndex(saveReaderIndex);
				break;
			} else {
				out.add(msg);
			}
		} while (input.isReadable());
	}
	
	/**
	 * @return Null if not full
	 */
	private ExchangeMessage decode(ByteBuf input) throws IOException {
		int readableBytes = input.readableBytes();
		if(readableBytes < Codec.HEADER) {
			return null;
		}
		
		ByteBuffer headerBuffer = ByteBuffer.allocate(Codec.HEADER);
		input.readBytes(headerBuffer);
		
		int bodyLength = headerBuffer.getInt(12);
		int bodyReadableBytes = input.readableBytes();
		if(bodyReadableBytes < bodyLength) {
			return null;
		}
		
		ByteBuffer bodyBuffer = ByteBuffer.allocate(bodyLength);
		input.readBytes(bodyBuffer);
		
		ExchangeMessage message = Codec.decode(headerBuffer, bodyBuffer);

		return message;
	}
}