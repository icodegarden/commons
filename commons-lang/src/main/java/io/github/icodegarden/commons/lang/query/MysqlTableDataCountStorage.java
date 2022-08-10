package io.github.icodegarden.commons.lang.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlTableDataCountStorage implements TableDataCountStorage {

	private static final String SQL_ADD = "insert into table_data_count (table_name,data_count,updated_at) values (?,?,?)";
	private static final String SQL_UPDATECOUNT = "update table_data_count set data_count = ?, updated_at = now() where table_name = ?";
	private static final String SQL_FINDALL = "select id,table_name,data_count from table_data_count";
	private static final String SQL_DELETE = "delete from table_data_count where id = ?";

	private final DataSource dataSource;

	public MysqlTableDataCountStorage(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	@Override
	public void add(TableDataCountPO po) throws DuplicateKeyException {
		try (Connection connection = dataSource.getConnection();) {
			try (PreparedStatement ptmt = connection.prepareStatement(SQL_ADD);) {
				ptmt.setString(1, po.getTableName());
				ptmt.setLong(2, po.getDataCount());
				ptmt.setString(3, SystemUtils.STANDARD_DATETIME_FORMATTER.format(po.getUpdatedAt()));

				ptmt.execute();
			}
		} catch (SQLException e) {
			if (e instanceof SQLIntegrityConstraintViolationException && e.getMessage().contains("Duplicate")) {
				throw new DuplicateKeyException(String.format("exist table:%s", po.getTableName()), e);
			}
			throw new IllegalStateException(String.format("insert table_data_count error, request:%s", po), e);
		}
	}

	@Override
	public int updateCount(String tableName, long count) {
		try (Connection connection = dataSource.getConnection();) {
			try (PreparedStatement ptmt = connection.prepareStatement(SQL_UPDATECOUNT);) {
				ptmt.setLong(1, count);
				ptmt.setString(2, tableName);

				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(
					String.format("update table_data_count error, tableName:%s, count:%d", tableName, count), e);
		}
	}

	@Override
	public List<TableDataCountPO> findAll() {
		try (Connection connection = dataSource.getConnection();) {
			try (PreparedStatement ptmt = connection.prepareStatement(SQL_FINDALL);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					List<TableDataCountPO> list = new LinkedList<TableDataCountPO>();
					while (rs.next()) {
						TableDataCountPO po = new TableDataCountPO();
						po.setId(rs.getString("id"));
						po.setTableName(rs.getString("table_name"));
						po.setDataCount(rs.getLong("data_count"));

						list.add(po);
					}
					return list;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("select table_data_count error"), e);
		}
	}

	int delete(String id) {
		try (Connection connection = dataSource.getConnection();) {
			try (PreparedStatement ptmt = connection.prepareStatement(SQL_DELETE);) {
				ptmt.setString(1, id);

				return ptmt.executeUpdate();
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("select table_data_count error"), e);
		}
	}
}