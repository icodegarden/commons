package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * <h1>支持Reentrant的互斥锁</h1> 只在相同对象、相同线程是可重入的，不同对象、不同线程一律互斥<br>
 * 
 * @author Fangfang.Xu
 *
 */
public interface DistributedReentrantLock extends DistributedLock {

}
