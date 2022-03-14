package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DistributedLock {

	boolean isAcquired() throws LockException;

	/**
	 * 阻塞直到获取到
	 */
	void acquire() throws LockException;

	/**
	 * 
	 * @param timeoutMillis 阻塞的时间
	 * @return 是否获取成功
	 */
	boolean acquire(long timeoutMillis) throws LockException;

	void release() throws LockException;
}
