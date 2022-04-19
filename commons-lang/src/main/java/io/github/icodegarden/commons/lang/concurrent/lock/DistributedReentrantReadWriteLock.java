package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * <h1>支持Reentrant的读写互斥锁</h1> <br>
 * 读锁全程共享，可重入<br>
 * 写锁全程互斥，可重入<br>
 * 读写全程互斥<br>
 * 
 * <br>
 * 
 * 在写锁中可以获取读锁，但读锁中获取写锁将永远不会成功<br>
 * 通过获取写锁，然后获取读锁，然后释放写锁，锁降级从写锁降级为读锁。但是，不可能从读锁升级到写锁<br>
 * 
 * @author Fangfang.Xu
 *
 */
public interface DistributedReentrantReadWriteLock {

	DistributedReentrantLock readLock();
	
	DistributedReentrantLock writeLock();
}
