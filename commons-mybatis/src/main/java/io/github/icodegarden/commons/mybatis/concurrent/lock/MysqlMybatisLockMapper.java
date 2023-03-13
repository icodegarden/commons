package io.github.icodegarden.commons.mybatis.concurrent.lock;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseLockDao;
import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcLock;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Mapper
public interface MysqlMybatisLockMapper extends DatabaseLockDao {

	/**
	 * 获取处于锁中的identifier
	 */
	@Select("<script> select identifier from " + MysqlJdbcLock.TABLE_NAME
			+ " where name = #{lockName} and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr}</script>")
	@Override
	String getLockedIdentifier(@Param("lockName") String lockName, @Param("nowStr") String nowStr);

	/**
	 * 锁数据是否存在
	 */
	@Select("<script> select id from " + MysqlJdbcLock.TABLE_NAME + " where name = #{lockName}</script>")
	@Override
	Long findRow(@Param("lockName") String lockName);

	@Insert("<script> insert into " + MysqlJdbcLock.TABLE_NAME
			+ " (`name`, `identifier`, `is_locked`, `expire_seconds`, `lock_at`) values(#{lockName}, #{identifier}, 1, #{expireSeconds}, #{lockAt})</script>")
	@Override
	void createRow(@Param("lockName") String lockName, @Param("identifier") String identifier,
			@Param("expireSeconds") Long expireSeconds, @Param("lockAt") String lockAt);

	@Update("<script> update " + MysqlJdbcLock.TABLE_NAME
			+ " set identifier=#{identifier},is_locked=1,expire_seconds=#{expireSeconds},lock_at=#{nowStr} where name=#{lockName} and ( is_locked = 0 or DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) &lt; #{nowStr})</script>")
	@Override
	int updateLocked(@Param("lockName") String lockName, @Param("identifier") String identifier,
			@Param("expireSeconds") Long expireSeconds, @Param("nowStr") String nowStr);

	@Update("<script> update " + MysqlJdbcLock.TABLE_NAME
			+ " set is_locked=0 where name=#{lockName}</script>")
	@Override
	int updateRelease(@Param("lockName") String lockName);
}
