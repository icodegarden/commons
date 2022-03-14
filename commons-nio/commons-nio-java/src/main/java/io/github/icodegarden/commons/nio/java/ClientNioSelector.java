package io.github.icodegarden.commons.nio.java;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ClientNioSelector implements Closeable {
	/**
	 * 
	 * @param name
	 * @return
	 * @throws IOException
	 */
	public static ClientNioSelector openNew(String name) throws ExceedExpectedRemoteException {
		ClientNioSelector clientNioSelector = new ClientNioSelector(name);
		try{
			clientNioSelector.start();
		} catch (IOException e) {
			throw new ExceedExpectedRemoteException(e);
		}
		return clientNioSelector;
	}
	
	private volatile boolean closed = true;
	private final String name;
	private Selector selector;

	private ClientNioSelector(String name) {
		this.name = name;
	}

	public void registerRead(ClientNioEventListener nioEventListener) throws ClosedChannelException {
		nioEventListener.socketChannel().register(selector, SelectionKey.OP_READ, nioEventListener);
	}

	private void start() throws IOException {
		selector = Selector.open();
		closed = false;
		
		new Thread("NioClient-Selector-Thread-" + name) {
			public void run() {
				while (!closed) {
					try {
						int count;
						if (SystemUtils.isWindowsPlatform()) {
							count = selector.select(100);
							System.out.print("");// slow
						} else {
							count = selector.select();
						}

						if (count > 0) {
							Set<SelectionKey> selectedKeys = selector.selectedKeys();
							Iterator<SelectionKey> iterator = selectedKeys.iterator();
							while (iterator.hasNext()) {
								SelectionKey key = iterator.next();
								try {
									if (key.isReadable()) {
										ClientNioEventListener nioEventListener = (ClientNioEventListener) key
												.attachment();
										nioEventListener.onRead(key);
									} else if (key.isWritable()) {
										//
									}
								} catch (Throwable e) {
									e.printStackTrace();
								} finally {
									iterator.remove();
								}
							}
						}
					} catch (Throwable e) {
						e.printStackTrace();
					}
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
