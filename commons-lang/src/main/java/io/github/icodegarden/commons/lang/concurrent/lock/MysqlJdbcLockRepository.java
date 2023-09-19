package io.github.icodegarden.commons.lang.concurrent.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class MysqlJdbcLockRepository implements DatabaseLockRepository {

	private final DataSource dataSource;

	public MysqlJdbcLockRepository(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public String getLockedIdentifier(String lockName, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select identifier from ").append(TABLE_NAME)
					.append(" where name = '").append(lockName).append("'")
					/*
					 * 要求is_locked=1 并且 锁没有过期
					 */
					.append(" and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) >= '").append(nowStr)
					.append("'").toString();
			if (log.isInfoEnabled()) {
				log.info("getLockedIdentifier sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						return rs.getString(1);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on getLockedIdentifier", e);
		}
	}

	@Override
	public Long findRow(String lockName) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(30).append("select id from ").append(TABLE_NAME).append(" where name = '")
					.append(lockName).append("'").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
//						String name = rs.getString(2);
						return id;
					}
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on existsRow", e);
		}
		return null;
	}

	@Override
	public void createRow(String lockName, String identifier, Long expireSeconds, String lockAt) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("insert into ").append(TABLE_NAME)
					.append(" (`name`, `identifier`, `is_locked`, `expire_seconds`, `lock_at`)")
					.append(" values(?, ?, 1, ?, ?) ").toString();
			if (log.isInfoEnabled()) {
				log.info("createRow sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, lockName);
				ptmt.setString(2, identifier);
				ptmt.setLong(3, expireSeconds);
				ptmt.setString(4, lockAt);

				ptmt.execute();
			}
		} catch (SQLException e) {
			DuplicateKeyException.throwIfCompatible(e);

			throw new IllegalStateException(String.format("ex on createRow"), e);
		}
	}

	@Override
	public int updateLocked(String lockName, String identifier, Long expireSeconds, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("update ").append(TABLE_NAME).append(" set identifier='")
					.append(identifier).append("',is_locked=1,expire_seconds='").append(expireSeconds)
					.append("',lock_at='").append(nowStr).append("' where name='").append(lockName)
					.append("' and (is_locked = 0 or DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) < '")
					.append(nowStr).append("')")//
					.toString();
			if (log.isInfoEnabled()) {
				log.info("acquire lock sql:{}", sql);
			}
			// DATE_ADD('2023-01-10 12:12:12',INTERVAL 11 SECOND) = '2023-01-10 12:12:23';
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateLocked", e);
		}
	}

	@Override
	public int updateRelease(String lockName) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(30).append("update ").append(TABLE_NAME).append(" set is_locked=0")
					.append(" where name='").append(lockName).append("'").toString();
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
