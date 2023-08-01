package io.github.icodegarden.commons.nio;

import java.io.Closeable;
import java.util.concurrent.Future;
import java.util.function.Consumer;

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

	/**
	 * async send only without response
	 */
	void send(Object body) throws RemoteException;

	/**
	 * request and wait response
	 */
	<R> R request(Object body) throws RemoteException;

	/**
	 * request and wait response until timeout
	 * 
	 * @param timeout millis
	 */
	<R> R request(Object body, int timeout) throws RemoteException;

	/**
	 * async request
	 */
	<R> Future<R> requestFuture(Object body) throws RemoteException;

	/**
	 * async request
	 * 
	 * 既有callback也有Future
	 */
	<R> Future<R> requestCallback(Object body, Consumer<R> successConsumer) throws RemoteException;

	boolean isClosed();

}
