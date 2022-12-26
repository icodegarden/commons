package io.github.icodegarden.commons.lang.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import io.github.icodegarden.commons.lang.dao.OptimizeTableResults.Result;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlJdbcDatabase implements JdbcDatabase {

	private final DataSource dataSource;

	public MysqlJdbcDatabase(DataSource dataSource) {
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
	public List<String> listTables() {
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
			throw new IllegalStateException(String.format("select count(0) error, %s", tableName), e);
		}
	}

	@Override
	public OptimizeTableResults<OptimizeTableResults.Result> optimizeTable(String tableName)
			throws IllegalStateException {
		/**
		 * Table Op Msg_type Msg_text {scheme.table} optimize note Table does not
		 * support optimize, doing recreate + analyze instead {scheme.table} optimize
		 * status OK
		 */
		OptimizeTableResults<Result> optimizeTableResults = new OptimizeTableResults<OptimizeTableResults.Result>();
		try (Connection connection = dataSource.getConnection();) {
			String sql = "OPTIMIZE TABLE " + tableName;
			try (PreparedStatement ptmt = connection.prepareStatement(sql);) {
				try (ResultSet rs = ptmt.executeQuery();) {
					while (rs.next()) {
						String Table = rs.getString(1);
						String Op = rs.getString(2);
						String Msg_type = rs.getString(3);
						String Msg_text = rs.getString(4);

						OptimizeTableResults.Result result = new OptimizeTableResults.Result();
						result.setTable(Table);
						result.setOp(Op);
						result.setMsg_type(Msg_type);
						result.setMsg_text(Msg_text);
						optimizeTableResults.add(result);
					}
				}
			}
			return optimizeTableResults;
		} catch (SQLException e) {
			throw new IllegalStateException(String.format("OPTIMIZE table error, %s", tableName), e);
		}
	}
}
