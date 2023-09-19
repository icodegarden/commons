package io.github.icodegarden.commons.lang.concurrent.registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class MysqlJdbcRegistryRepository implements DatabaseRegistryRepository<Long> {

	private final DataSource dataSource;

	public MysqlJdbcRegistryRepository(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public SimpleDO<Long> findByRegistration(Registration registration, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select id,index from ").append(TABLE_NAME)
					.append(" where identifier = '").append(registration.getIdentifier()).append("'")
					.append(" and name='").append(registration.getName()).append("'")
					/*
					 * 要求is_registered=1 并且 没有过期
					 */
					.append(" and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) >= '")
					.append(nowStr).append("'").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findByRegistration", e);
		}
	}

	@Override
	public SimpleDO<Long> findAnyAvailableByName(String name, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select id,index from ").append(TABLE_NAME)
					.append(" where name = '").append(name).append("'")
					/*
					 * 要求is_registered=0 或 已过期
					 */
					.append(" and is_registered=0 OR DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) < '")
					.append(nowStr).append("'").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findAnyAvailableByName", e);
		}
	}

	@Override
	public SimpleDO<Long> findLastByName(String name) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select id,index from ").append(TABLE_NAME)
					.append(" where name = '").append(name).append("' order by index desc").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findLastByName", e);
		}
	}

	@Override
	public void createOnRegister(int index, Registration registration) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("insert into ").append(TABLE_NAME).append(
					" (`name`, `identifier`, `index`, `is_registered`, `metadata`, `info`, `expire_seconds`, `lease_at`)")
					.append(" values(?, ?, ?, 1, ?, ?, ?, ?) ").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, registration.getName());
				ptmt.setString(2, registration.getIdentifier());
				ptmt.setInt(3, index);
				ptmt.setString(4,
						registration.getMetadata() != null ? JsonUtils.serialize(registration.getMetadata()) : "{}");
				ptmt.setString(5, registration.getInfo() != null ? JsonUtils.serialize(registration.getInfo()) : "{}");
				ptmt.setLong(6, registration.getExpireSeconds());
				ptmt.setString(7, SystemUtils.STANDARD_DATETIME_FORMATTER.format(SystemUtils.now()));

				ptmt.execute();
			}
		} catch (SQLException e) {
			DuplicateKeyException.throwIfCompatible(e);

			throw new IllegalStateException(String.format("ex on createOnRegister"), e);
		}
	}

	@Override
	public void updateOnRegister(Long id, Registration registration) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateOnDeregister(Registration registration) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean updateLease(Registration registration) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void updateRegistration(Long id, Registration registration) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Registration> findAllRegistered(String name) {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public String getLockedIdentifier(String lockName, String nowStr) {
//		try (Connection connection = dataSource.getConnection();) {
//			String sql = new StringBuilder(100).append("select identifier from ").append(TABLE_NAME)
//					.append(" where name = '").append(lockName).append("'")
//					/*
//					 * 要求is_locked=1 并且 锁没有过期
//					 */
//					.append(" and is_locked=1 and DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) >= '").append(nowStr)
//					.append("'").toString();
//			if (log.isInfoEnabled()) {
//				log.info("getLockedIdentifier sql:{}", sql);
//			}
//			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
//				try (ResultSet rs = ptmt.executeQuery();) {
//					while (rs.next()) {
//						return rs.getString(1);
//					}
//				}
//			}
//			return null;
//		} catch (SQLException e) {
//			throw new IllegalStateException("ex on getLockedIdentifier", e);
//		}
//	}
//
//	@Override
//	public Long findRow(String lockName) {
//		try (Connection connection = dataSource.getConnection();) {
//			String sql = new StringBuilder(30).append("select id from ").append(TABLE_NAME).append(" where name = '")
//					.append(lockName).append("'").toString();
//			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
//				try (ResultSet rs = ptmt.executeQuery();) {
//					while (rs.next()) {
//						long id = rs.getLong(1);
////						String name = rs.getString(2);
//						return id;
//					}
//				}
//			}
//		} catch (SQLException e) {
//			throw new IllegalStateException("ex on existsRow", e);
//		}
//		return null;
//	}
//
//	@Override
//	public void createRow(String lockName, String identifier, Long expireSeconds, String lockAt) {
//		try (Connection connection = dataSource.getConnection();) {
//			String sql = new StringBuilder(100).append("insert into ").append(TABLE_NAME)
//					.append(" (`name`, `identifier`, `is_locked`, `expire_seconds`, `lock_at`)")
//					.append(" values(?, ?, 1, ?, ?) ").toString();
//			if (log.isInfoEnabled()) {
//				log.info("createRow sql:{}", sql);
//			}
//			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
//				ptmt.setString(1, lockName);
//				ptmt.setString(2, identifier);
//				ptmt.setLong(3, expireSeconds);
//				ptmt.setString(4, lockAt);
//
//				ptmt.execute();
//			}
//		} catch (SQLException e) {
//			DuplicateKeyException.throwIfCompatible(e);
//
//			throw new IllegalStateException(String.format("ex on createRow"), e);
//		}
//	}
//
//	@Override
//	public int updateLocked(String lockName, String identifier, Long expireSeconds, String nowStr) {
//		try (Connection connection = dataSource.getConnection();) {
//			String sql = new StringBuilder(100).append("update ").append(TABLE_NAME).append(" set identifier='")
//					.append(identifier).append("',is_locked=1,expire_seconds='").append(expireSeconds)
//					.append("',lock_at='").append(nowStr).append("' where name='").append(lockName)
//					.append("' and (is_locked = 0 or DATE_ADD(lock_at,INTERVAL expire_seconds SECOND) < '")
//					.append(nowStr).append("')")//
//					.toString();
//			if (log.isInfoEnabled()) {
//				log.info("acquire lock sql:{}", sql);
//			}
//			// DATE_ADD('2023-01-10 12:12:12',INTERVAL 11 SECOND) = '2023-01-10 12:12:23';
//			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
//				return ptmt.executeUpdate();
//			}
//		} catch (SQLException e) {
//			throw new IllegalStateException("ex on updateLocked", e);
//		}
//	}
//
//	@Override
//	public int updateRelease(String lockName) {
//		try (Connection connection = dataSource.getConnection();) {
//			String sql = new StringBuilder(30).append("update ").append(TABLE_NAME).append(" set is_locked=0")
//					.append(" where name='").append(lockName).append("'").toString();
//			if (log.isInfoEnabled()) {
//				log.info("release lock sql:{}", sql);
//			}
//			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
//				return ptmt.executeUpdate();
//			}
//		} catch (SQLException e) {
//			throw new IllegalStateException("ex on updateRelease", e);
//		}
//	}

}
