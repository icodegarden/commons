package io.github.icodegarden.commons.mybatis.concurrent.lock;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseLockRepository;
import io.github.icodegarden.commons.lang.concurrent.lock.DatabaseReadWriteLockRepository;
import io.github.icodegarden.commons.lang.concurrent.lock.MysqlJdbcReadWriteLockRepository;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Mapper
public interface MysqlMybatisReadWriteLockMapper extends DatabaseReadWriteLockRepository {

	/**
	 * 获取处于锁中的identifier
	 */
	@Select("<script> select identifier,is_read_type from " + MysqlJdbcReadWriteLockRepository.TABLE_NAME
			+ " where name = #{lockName} and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr}</script>")
	@Override
	List<LockDO> listLockedDatas(@Param("lockName") String lockName, @Param("nowStr") String nowStr);

	/**
	 * 获取处于锁中的identifier
	 */
	@Select("<script> select identifier,is_read_type from " + MysqlJdbcReadWriteLockRepository.TABLE_NAME
			+ " where name = #{lockName} and identifier=#{identifier} and is_read_type=#{readType} and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr}</script>")
	@Override
	List<LockDO> listLockedDataInterProcess(@Param("lockName") String lockName, @Param("identifier") String identifier,
			@Param("readType") boolean readType, @Param("nowStr") String nowStr);

	@Insert("<script> insert into " + MysqlJdbcReadWriteLockRepository.TABLE_NAME
			+ " (`name`, `identifier`, `is_read_type`, `is_locked`, `expire_seconds`, `lock_at`) values(#{lockName}, #{identifier},  #{readType}, 1, #{expireSeconds}, #{lockAt})</script>")
	@Override
	void createRow(@Param("lockName") String lockName, @Param("identifier") String identifier,
			@Param("expireSeconds") Long expireSeconds, @Param("lockAt") String lockAt,
			@Param("readType") boolean readType);

	@Delete("<script> delete from " + MysqlJdbcReadWriteLockRepository.TABLE_NAME
			+ " where name=#{lockName} and identifier=#{identifier}</script>")
	@Override
	int deleteRow(@Param("lockName") String lockName, @Param("identifier") String identifier);
}
