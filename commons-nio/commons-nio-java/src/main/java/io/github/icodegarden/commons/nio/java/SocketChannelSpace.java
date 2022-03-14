package io.github.icodegarden.commons.nio.java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.nio.Codec;
import io.github.icodegarden.commons.nio.ExchangeMessage;

/**
 * NOT THREAD SAFE<br>
 * 每个对应的连接都需要一个空间，无论client、serverSideClient
 * @author Fangfang.Xu
 *
 */
class SocketChannelSpace {
	private static final Logger log = LoggerFactory.getLogger(SocketChannelSpace.class);
	
	private final String name;
	private final ByteBuffer headerBuffer = ByteBuffer.allocate(Codec.HEADER);
	private ByteBuffer bodyBuffer;
	
	public SocketChannelSpace(String name) {
		this.name = name;
	}
	
	public void write(SocketChannel socketChannel, ExchangeMessage message) throws IOException {
		ByteBuffer buffer = Codec.encode(message);
		write(socketChannel, buffer);
	}

	private void write(SocketChannel socketChannel, ByteBuffer buffer) throws IOException {
		buffer.flip();// 必须，flip后才能读
		/**
		 * channel的写操作在并发时，必须保证顺序写入，否则字节错乱
		 */
		synchronized (this) {
			while (buffer.hasRemaining()) {
				socketChannel.write(buffer);
			}
		}
	}

	/**
	 * @return Null if not full
	 */
	public ExchangeMessage read(SocketChannel channel) throws IOException {
		if (bodyBuffer == null) {
			if (log.isDebugEnabled()) {
				log.debug("{} reading header...,channel:{}",channel);
			}
			
			channel.read(headerBuffer);
			if (headerBuffer.position() == 0) {// 当client close时，server 会收到read，但读不到数据
				throw new ClosedChannelException();// 意味着client close了
			}
			// read header
			if (headerBuffer.position() < Codec.HEADER) {
				if (log.isDebugEnabled()) {
					log.debug("{} position not at header length, continue reading...",name);
				}
				return null;
			}
//			headerBuffer.flip();get指定index不用flip

			int bodyLength = headerBuffer.getInt(12);

			bodyBuffer = ByteBuffer.allocate(bodyLength);
			return null;
		} else {
			channel.read(bodyBuffer);
//			buffer.position(headerBuffer.limit());//使用put position不会自动加

			if (bodyBuffer.position() < bodyBuffer.limit()) {
				if (log.isDebugEnabled()) {
					log.debug("{} position not reach body length, continue read...", name);
				}
				return null;
			}
			
			
			ExchangeMessage message = Codec.decode(headerBuffer, bodyBuffer);
			
			headerBuffer.clear();
			bodyBuffer = null;

			return message;
		}
	}

}
