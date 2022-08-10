package io.github.icodegarden.commons.lang.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlTableDataCountCollector extends AbstractTableDataCountCollector {

	private final DataSource dataSource;

	public MysqlTableDataCountCollector(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public MysqlTableDataCountCollector(DataSource dataSource, Set<String> whiteListTables) {
		super(whiteListTables);
		this.dataSource = dataSource;
	}

	@Override
	public String version() {
		try (Connection connection = dataSource.getConnection();) {
			String sql = "select version() as version";
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						return rs.getString("version");
					}

					return null;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("select version() error"), e);
		}
	}

	@Override
	public List<String> doListTables() {
		try (Connection connection = dataSource.getConnection();) {
			String sql = "show tables";
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					List<String> list = new LinkedList<String>();
					while (rs.next()) {
						list.add(rs.getString(1));
					}

					return list;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("show tables error"), e);
		}
	}

	@Override
	public long countTable(String tableName) {
		try (Connection connection = dataSource.getConnection();) {
			String sql = "select count(0) from " + tableName;// 如果使用?，最后会是select count(0) from 'xxx' 这是错误的
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						return rs.getLong(1);
					}

					return 0;
				}
			}
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("select count(0) error"), e);
		}
	}

}