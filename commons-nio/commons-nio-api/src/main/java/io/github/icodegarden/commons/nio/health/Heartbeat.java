package io.github.icodegarden.commons.nio.health;

import java.io.Closeable;
import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Heartbeat extends Closeable {

	void send() throws IOException;

	void receive();

	void response() throws IOException;

	long lastSend();

	long lastReceive();

	void reconnect() throws IOException;

	boolean isClosed();
}
