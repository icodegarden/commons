package io.github.icodegarden.commons.nio;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.exception.remote.ExceedExpectedRemoteException;
import io.github.icodegarden.commons.lang.exception.remote.RemoteException;
import io.github.icodegarden.commons.lang.exception.remote.TimeoutRemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AbstractNioClient implements NioClient {

	private static final Logger log = LoggerFactory.getLogger(AbstractNioClient.class);

	private static final int DEFAULT_CONNECT_TIMEOUT = 3000;
	private static final int DEFAULT_REQUEST_TIMEOUT = 3000;

	protected int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	protected int requestTimeout = DEFAULT_REQUEST_TIMEOUT;
	private SerializerType serializerType = SerializerType.Hessian2;

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	
	/**
	 * @param requestTimeout 默认的请求超时时间
	 */
	public void setRequestTimeout(int requestTimeout) {
		this.requestTimeout = requestTimeout;
	}
	
	public void setSerializerType(SerializerType serializerType) {
		this.serializerType = serializerType;
	}

	@Override
	public void send(Object body) throws RemoteException {
		ExchangeMessage message = new ExchangeMessage(true, false, false, serializerType.getValue(), body);
		doSend(message);
	}

	@Override
	public <R> R request(Object body) throws RemoteException {
		return (R) request(body, requestTimeout);
	}

	@Override
	public <R> R request(Object body, int timeout) throws RemoteException {
		ExchangeMessage message = new ExchangeMessage(true, true, false, serializerType.getValue(), body);
		long requestId = message.getRequestId();
		Future future = new Future(requestId);
		try {
			doSend(message);
			return (R) future.get(timeout);
		} catch (RemoteException e) {
			throw e;
		} catch (Exception e) {
			throw new ExceedExpectedRemoteException(e);
		} finally {
			future.remove();
		}
	}

	protected abstract void doSend(ExchangeMessage message) throws RemoteException;

	public static class Future {
		private static final Map<Long/* requestId */, Future> FUTURES = new ConcurrentHashMap<>();
		private ReentrantLock lock = new ReentrantLock();
		private Condition done = lock.newCondition();

		private Long requestId;
		private Object val;

		Future(Long requestId) {
			this.requestId = requestId;
			FUTURES.put(requestId, this);
		}

		public static void received(Long requestId, Object obj) {
			Future future = FUTURES.remove(requestId);
			if (future != null) {
				future.doReceived(obj);
			}
		}

		Object get(int timeout) {
			if (timeout <= 0) {
				timeout = 1000;
			}
			if (!isDone()) {
				long start = System.currentTimeMillis();
				lock.lock();
				try {
					while (!isDone()) {
						done.await(timeout, TimeUnit.MILLISECONDS);
						if (isDone() || System.currentTimeMillis() - start > timeout) {
							break;
						}
					}
				} catch (InterruptedException e) {
					/**
					 * 当用户中断等待时，直接返回，无论是否有值
					 */
					return val;
				} finally {
					lock.unlock();
				}
				if (!isDone()) {
					throw new TimeoutRemoteException("timeout after wait " + timeout);
				}
			}
			return val;
		}

		void doReceived(Object val) {
			lock.lock();
			try {
				this.val = val;
				if (done != null) {
					done.signal();
				}
			} finally {
				lock.unlock();
			}
		}

		boolean isDone() {
			return val != null;
		}

		void remove() {
			Future future = FUTURES.remove(requestId);
			if (future != null && log.isInfoEnabled()) {
				boolean done = future.isDone();
				if (done) {
					if (log.isDebugEnabled()) {
						log.debug("Future of requestId:{} is removed", requestId);
					}
				} else {
					if (log.isWarnEnabled()) {
						log.warn("Future of requestId:{} is removed and not done", requestId);
					}
				}
			}
		}
	}
}