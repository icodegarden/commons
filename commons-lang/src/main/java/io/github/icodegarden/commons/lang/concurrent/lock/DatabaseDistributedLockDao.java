package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DatabaseDistributedLockDao {

	public static final String TABLE_NAME = "distributed_lock";

	/**
	 * 获取处于锁中的identifier
	 */
	String getLockedIdentifier(String lockName, String nowStr);

	/**
	 * 锁数据是否存在
	 */
	int existsRow(String lockName);

	void createRow(String lockName, String identifier, Long expireSeconds, String lockAt);

	int updateLocked(String lockName, String identifier, Long expireSeconds, String nowStr);

	int updateRelease(String lockName);
}
