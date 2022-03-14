package io.github.icodegarden.commons.nio;

import java.io.Closeable;

import io.github.icodegarden.commons.lang.exception.remote.ConnectFailedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface NioClient extends Closeable {

	void connect() throws ConnectFailedRemoteException;

	void reconnect() throws ConnectFailedRemoteException;

	void send(Object body) throws RemoteException;

	<R> R request(Object body) throws RemoteException;

	/**
	 * 
	 * @param <R>
	 * @param body
	 * @param timeout millis
	 * @return
	 */
	<R> R request(Object body, int timeout) throws RemoteException;

	boolean isClosed();

}
