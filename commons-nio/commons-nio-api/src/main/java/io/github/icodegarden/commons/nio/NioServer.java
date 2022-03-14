package io.github.icodegarden.commons.nio;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface NioServer extends Closeable {

	void start() throws IOException;

	boolean isClosed();
}
