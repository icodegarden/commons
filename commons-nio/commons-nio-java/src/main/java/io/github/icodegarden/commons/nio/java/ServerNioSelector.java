package io.github.icodegarden.commons.nio.java;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
class ServerNioSelector implements Closeable {
	private static Logger log = LoggerFactory.getLogger(ServerNioSelector.class);

	public static ServerNioSelector openNew(String name) throws IOException {
		ServerNioSelector serverNioSelector = new ServerNioSelector(name);
		serverNioSelector.open();
		return serverNioSelector;
	}

	private volatile boolean closed = true;
	private Selector selector;
//	private SelectionKey serverKey;
	private ServerNioEventListener serverNioEventListener;

	private String name;

	private ServerNioSelector(String name) {
		this.name = name;
	}

	private void open() throws IOException {
		selector = Selector.open();
		closed = false;
	}

	public void registerAccept(ServerNioEventListener serverNioEventListener) throws ClosedChannelException {
		this.serverNioEventListener = serverNioEventListener;
		serverNioEventListener.serverSocketChannel().register(selector, SelectionKey.OP_ACCEPT);
	}

	public void registerRead(ServerSideClient serverSide) throws ClosedChannelException {
		serverSide.socketChannel().register(selector, SelectionKey.OP_READ, serverSide);
	}

	/**
	 * selector 单线程loop
	 * 
	 * 每个channel 读事件 -> 按序读，不能并发读（否则字节顺序会错乱） -> #一次等待读满（阻塞其他channel的读事件）
	 * #read一下就结束（没满就等待下一次的读事件，满了就可以处理事务）
	 * 
	 * 每个channel 写事件 -> 对每个消息按序写，不能并发写（否则字节顺序会错乱） ->
	 * #在read事件中同步（包括read直接write、read/write兴趣切换）处理业务并写完 #通过线程池处理业务和写入
	 */
	public void start() throws IOException {
		new Thread("NioServer-Selector-Thread-" + name) {
			public void run() {
				while (!closed) {
					try {
						int count = selector.select();
						if (count > 0) {
							Set<SelectionKey> selectedKeys = selector.selectedKeys();
							Iterator<SelectionKey> iterator = selectedKeys.iterator();
							while (iterator.hasNext()) {
								SelectionKey key = iterator.next();
								try {
									if (key.isAcceptable()) {
										serverNioEventListener.onAccept();
									} else if (key.isReadable()) {
										ServerSideClient serverSide = (ServerSideClient) key.attachment();
										serverSide.onRead(key);
									} else if (key.isWritable()) {
										ServerSideClient serverSide = (ServerSideClient) key.attachment();
										serverSide.onWrite(key);
									}
								} catch (Throwable e) {
									log.error("ex in SelectionKey handle, {}, that means client was closed", name, e);
									//客户端已主动关闭
									ServerSideClient serverSide = (ServerSideClient) key.attachment();
									serverSide.close();
								} finally {
									iterator.remove();
								}
							}
						}
					} catch (Throwable e) {
						log.error("ex in select handle, {}", name, e);
					}
				}
				try {
					selector.close();
				} catch (IOException ignore) {
					log.error("ex on close Selector, {}", name, ignore);
				}
			};
		}.start();
	}

	@Override
	public void close() throws IOException {
		closed = true;
		selector.wakeup();
	}
}
