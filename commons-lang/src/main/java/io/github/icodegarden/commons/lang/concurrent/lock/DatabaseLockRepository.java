package io.github.icodegarden.commons.lang.concurrent.lock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DatabaseLockRepository {

	public static final String TABLE_NAME = "distributed_lock";

	/**
	 * 锁数据是否存在
	 * @return row id
	 */
	Long findRow(String lockName);

	void createRow(String lockName, String identifier, Long expireSeconds, String lockAt);

	/**
	 * 获取处于锁中的identifier
	 */
	String getLockedIdentifier(String lockName, String nowStr);
	
	int updateLocked(String lockName, String identifier, Long expireSeconds, String nowStr);

	int updateRelease(String lockName);
}
