package io.github.icodegarden.commons.lang.concurrent.registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.exception.DuplicateKeyException;
import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcRegistryRepository implements DatabaseRegistryRepository<Long> {

	private final DataSource dataSource;

	public MysqlJdbcRegistryRepository(DataSource dataSource) {
		super();
		this.dataSource = dataSource;
	}

	@Override
	public SimpleRegistrationDO<Long> findByRegistration(Registration registration, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(200)//
					.append("select id,`index` from ").append(TABLE_NAME)//
					.append(" where identifier = '").append(registration.getIdentifier()).append("'")//
					.append(" and name='").append(registration.getName()).append("'")//
					/*
					 * 要求is_registered=1 并且 没有过期
					 */
					.append(" and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) >= '")
					.append(nowStr).append("' limit 1")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleRegistrationDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findByRegistration", e);
		}
	}

	@Override
	public SimpleRegistrationDO<Long> findAnyAvailableByName(String name, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(200).append("select id,`index` from ").append(TABLE_NAME)//
					.append(" where name = '").append(name).append("'")//
					/*
					 * 要求is_registered=0 或 已过期
					 */
					.append(" and (is_registered=0 OR DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) < '")
					.append(nowStr).append("') limit 1")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleRegistrationDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findAnyAvailableByName", e);
		}
	}

	@Override
	public SimpleRegistrationDO<Long> findMaxIndexByName(String name) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("select id,`index` from ").append(TABLE_NAME)
					.append(" where name = '").append(name).append("' order by `index` desc limit 1").toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						long id = rs.getLong(1);
						int index = rs.getInt(2);
						return new SimpleRegistrationDO<Long>(id, index);
					}
				}
			}
			return null;
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findLastByName", e);
		}
	}

	@Override
	public void createOnRegister(int index, Registration registration, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100)//
					.append("insert into ").append(TABLE_NAME)//
					.append(" (`name`, `identifier`, `index`, `is_registered`, `metadata`, `info`, `expire_seconds`, `lease_at`)")//
					.append(" values(?, ?, ?, 1, ?, ?, ?, ?) ")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, registration.getName());
				ptmt.setString(2, registration.getIdentifier());
				ptmt.setInt(3, index);
				ptmt.setString(4,
						registration.getMetadata() != null ? JsonUtils.serialize(registration.getMetadata()) : "{}");
				ptmt.setString(5, registration.getInfo() != null ? JsonUtils.serialize(registration.getInfo()) : "{}");
				ptmt.setLong(6, registration.getExpireSeconds());
				ptmt.setString(7, nowStr);

				ptmt.execute();
			}
		} catch (SQLException e) {
			DuplicateKeyException.throwIfCompatible(e);

			throw new IllegalStateException(String.format("ex on createOnRegister"), e);
		}
	}

	@Override
	public void updateOnRegister(Long id, Registration registration, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100)//
					.append("update ").append(TABLE_NAME)//
					.append(" set name=?,identifier=?,is_registered=1,expire_seconds=?,lease_at=? where id=?")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, registration.getName());
				ptmt.setString(2, registration.getIdentifier());
				ptmt.setLong(3, registration.getExpireSeconds());
				ptmt.setString(4, nowStr);
				ptmt.setLong(5, id);

				ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateOnRegister", e);
		}
	}

	@Override
	public void updateOnDeregister(Long id) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100).append("update ").append(TABLE_NAME)
					.append(" set is_registered=0 where id=").append(id).toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateOnDeregister", e);
		}
	}

	@Override
	public int updateLease(Registration registration, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100)//
					.append("update ").append(TABLE_NAME)//
					.append(" set lease_at=?")//
					/*
					 * 要求is_registered=1 并且 没有过期
					 */
					.append(" where identifier = ? and name=? and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) >= ? ")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, nowStr);
				ptmt.setString(2, registration.getIdentifier());
				ptmt.setString(3, registration.getName());
				ptmt.setString(4, nowStr);

				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateLease", e);
		}
	}

	@Override
	public void updateRegistration(Long id, String metadata, String info) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = new StringBuilder(100)//
					.append("update ").append(TABLE_NAME)//
					.append(" set metadata=?,info=?")//
					.append(" where id = ?")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, metadata);
				ptmt.setString(2, info);
				ptmt.setLong(3, id);

				ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on updateRegistration", e);
		}
	}

	@Override
	public List<Registration> findAllRegistered(String name, boolean withMetadata, boolean withInfo, String nowStr) {
		try (Connection connection = dataSource.getConnection();) {
			StringBuilder sb = new StringBuilder(200)//
					.append("select id,identifier,`index`,expire_seconds");
			if (withMetadata) {
				sb.append(",metadata");
			}
			if (withInfo) {
				sb.append(",info");
			}

			String sql = sb.append(" from ").append(TABLE_NAME)//
					/*
					 * 要求is_registered=1 并且 没有过期
					 */
					.append(" where name = ? and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) >=?")//
					.toString();
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				ptmt.setString(1, name);
				ptmt.setString(2, nowStr);

				List<Registration> list = new LinkedList<>();

				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
//						long id = rs.getLong(1);
						String identifier = rs.getString(2);
//						int index= rs.getInt(3);
						long expireSeconds = rs.getLong(4);
						String metadata = null;
						String info = null;
						if (withMetadata) {
							metadata = rs.getString(4);
						}
						if (withInfo) {
							info = rs.getString(5);
						}

						Registration registration = new Registration.Default(name, identifier, expireSeconds, metadata,
								info);
						list.add(registration);
					}
				}
				return list;
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on findAllRegistered", e);
		}
	}

}
