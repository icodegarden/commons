package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DistributedReentrantReadWriteLock {

	DistributedReentrantLock readLock();
	
	DistributedReentrantLock writeLock();
}
