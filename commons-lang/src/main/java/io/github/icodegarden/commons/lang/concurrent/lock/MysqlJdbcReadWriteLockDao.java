package io.github.icodegarden.commons.lang.concurrent.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class MysqlJdbcReadWriteLockDao implements DatabaseReadWriteLockDao {

	private final DataSource dataSource;

	public MysqlJdbcReadWriteLockDao(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public List<LockDO> listLocks(String lockName, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select identifier,is_read_type from ").append(TABLE_NAME)
					.append(" where name = '").append(lockName).append("'")
					/*
					 * 要求is_locked=1 并且 锁没有过期
					 */
					.append(" and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) >= '").append(nowStr)
					.append("'").toString();
			if (log.isInfoEnabled()) {
				log.info("listLocks sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					List<LockDO> list = new LinkedList<LockDO>();
					while (rs.next()) {
						String identifier = rs.getString(1);
						boolean readType = rs.getBoolean(2);
						LockDO lockDO = new LockDO(identifier, readType);
						list.add(lockDO);
					}
					return list;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on getLockedIdentifier", e);
		}
	}

	@Override
	public void createRow(String lockName, String identifier, Long expireSeconds, String lockAt, boolean readType) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("insert into ").append(TABLE_NAME)
					.append(" (`name`, `identifier`, `is_locked`, `is_read_type`, `expire_seconds`, `lock_at`)")
					.append(" values(?, ?, 1, ?, ?, ?) ").toString();
			if (log.isInfoEnabled()) {
				log.info("createRow sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, lockName);
				ptmt.setString(2, identifier);
				ptmt.setBoolean(3, readType);
				ptmt.setLong(4, expireSeconds);
				ptmt.setString(5, lockAt);

				ptmt.execute();
			}
		} catch (SQLException e) {
			DuplicateKeyException.throwIfCompatible(e);

			throw new IllegalStateException(String.format("ex on createRow"), e);
		}
	}

	@Override
	public int deleteRow(String lockName, String identifier) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(30).append("delete from ").append(TABLE_NAME).append(" where name='")
					.append(lockName).append("'").append(" and identifier='").append(identifier).append("'").toString();
			if (log.isInfoEnabled()) {
				log.info("release lock sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateRelease", e);
		}
	}
}
