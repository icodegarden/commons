package io.github.icodegarden.commons.mybatis.concurrent.lock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.concurrent.lock.LockExceedExpectedException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockException;
import io.github.icodegarden.commons.lang.concurrent.lock.LockInterruptedException;
import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class MysqlMybatisDistributedLock implements MybatisDistributedLock {

	private static final String TABLE_NAME = "distributed_lock";

	private final DataSource dataSource;

	private final String identifier = UUID.randomUUID().toString();

	private final String name;
	private final Long expireSeconds;

	private long acquireIntervalMillis = 500;

	public MysqlMybatisDistributedLock(MysqlMybatisDistributedLockMapper mapper, String name, Long expireSeconds) {
		Assert.hasText(name, "name must not empty");
		Assert.isTrue(name.length() <= 20, "name length must <= 20");
		
		this.dataSource = dataSource;
		this.name = name;
		this.expireSeconds = expireSeconds;
	}

	public void setAcquiredIntervalMillis(long acquireIntervalMillis) {
		Assert.isTrue(acquireIntervalMillis > 0, "acquireIntervalMillis must gt 0");
		this.acquireIntervalMillis = acquireIntervalMillis;
	}

	@Override
	public boolean isAcquired() throws LockException {
		try {
			String value = getLockedIdentifier();
			return value != null && value.equals(identifier);
		} catch (Exception e) {
			throw new LockExceedExpectedException(e);
		}
	}

	@Override
	public void acquire() throws LockException {
		acquire(Long.MAX_VALUE);
	}

	/**
	 * 先查询lock name对应的数据是否已存在<br>
	 * 存在则使用update ... where name=? and (is_locked = 0 or
	 * lock_at+timeout<now )更新，更新结果=0说明获取锁失败，否则成功<br> 
	 * 不存在则新增，新增被唯一约束说明获取锁失败，否则成功<br>
	 */
	@Override
	public boolean acquire(long timeoutMillis) throws LockException {
		LocalDateTime start = SystemUtils.now();
		for (;;) {
			try {
				if (existsRow()) {
					try (Connection connection = dataSource.getConnection();) {
						String nowStr = SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now());

						String sql = new StringBuilder(100).append("update ").append(TABLE_NAME)
								.append(" set identifier='").append(identifier).append("',is_locked=1,expire_seconds='")
								.append(expireSeconds).append("',lock_at='").append(nowStr).append("' where name='")
								.append(name)
								.append("' and (is_locked = 0 or DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) < '")
								.append(nowStr).append("')")//
								.toString();
						if(log.isInfoEnabled()) {
							log.info("acquire lock sql:{}", sql);
						}
						// DATE_ADD('2023-01-10 12:12:12',INTERVAL 11 SECOND) = '2023-01-10 12:12:23';
						try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
							int rows = ptmt.executeUpdate();
							if (rows == 1) {
								return true;
							}
						}
					}
				} else {
					try {
						createRow();
						return true;
					} catch (DuplicateKeyException e) {
						// 继续下一轮
					}
				}

				if (SystemUtils.now().minus(timeoutMillis, ChronoUnit.MILLIS).isAfter(start)) {
					return false;
				}
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
			sleep();
		}
	}

	@Override
	public void release() throws LockException {
		/**
		 * 必须要检查
		 */
		if (isAcquired()) {
			try {
				try (Connection connection = dataSource.getConnection();) {
					String sql = new StringBuilder(30).append("update ").append(TABLE_NAME).append(" set is_locked=0")
							.append(" where name='").append(name).append("'").toString();
					if(log.isInfoEnabled()) {
						log.info("release lock sql:{}", sql);
					}
					try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
						ptmt.executeUpdate();
					}
				}
			} catch (Exception e) {
				throw new LockExceedExpectedException(e);
			}
		}
	}

	/**
	 * 获取处于锁中的identifier
	 */
	private String getLockedIdentifier() throws SQLException {
		try (Connection connection = dataSource.getConnection();) {
			String nowStr = SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now());

			String sql = new StringBuilder(100).append("select identifier from ").append(TABLE_NAME)
					.append(" where name = '").append(name).append("'")
					/*
					 * 要求is_locked=1 并且 锁没有过期
					 */
					.append(" and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) >= '").append(nowStr)
					.append("'").toString();
			if(log.isInfoEnabled()) {
				log.info("getLockedIdentifier sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						return rs.getString(1);
					}
				}
			}
		}
		return null;
	}

	/**
	 * 锁数据是否存在
	 */
	private boolean existsRow() throws SQLException {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(30).append("select id from ").append(TABLE_NAME).append(" where name = '")
					.append(name).append("'").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
//						long id = rs.getLong(1);
//						String name = rs.getString(2);
						return true;
					}
				}
			}
		}
		return false;
	}

	private void createRow() throws DuplicateKeyException {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("insert into ").append(TABLE_NAME)
					.append(" (`name`, `identifier`, `is_locked`, `expire_seconds`, `lock_at`)")
					.append(" values(?, ?, 1, ?, ?) ").toString();
			if(log.isInfoEnabled()) {
				log.info("createRow sql:{}", sql);
			}
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, name);
				ptmt.setString(2, identifier);
				ptmt.setLong(3, expireSeconds);
				ptmt.setString(4, SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));

				ptmt.execute();
			}
		} catch (SQLException e) {
			DuplicateKeyException.throwIfCompatible(e);

			throw new IllegalStateException(String.format("createLockData error"), e);
		}
	}

	private void sleep() throws LockInterruptedException {
		try {
			Thread.sleep(acquireIntervalMillis);
		} catch (InterruptedException e) {
			throw new LockInterruptedException(e);
		}
	}
}
