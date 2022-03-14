package io.github.icodegarden.commons.nio.java;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ClientNioEventListener {

	SocketChannel socketChannel();

	void onRead(SelectionKey key) throws IOException;

	void onWrite(SelectionKey key) throws IOException;
}
