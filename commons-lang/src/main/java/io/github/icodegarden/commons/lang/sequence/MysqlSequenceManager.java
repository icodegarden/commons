package io.github.icodegarden.commons.lang.sequence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.util.Assert;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlSequenceManager extends AtomicSequenceManager {

	private DataSource dataSource;

	private final long increment;

	private final String getIncrementSql;
//	private final String currentIdSql;
	private final String nextMaxIdSql;

	public MysqlSequenceManager(String moduleName, DataSource dataSource) {
		super(moduleName);
		Assert.notNull(dataSource, "dataSource must not null");
		this.dataSource = dataSource;

		getIncrementSql = "select `increment` from `id_sequence` where name = ('" + moduleName + "')";
//		currentIdSql = "select id_seq_currval('" + moduleName + "')";
		nextMaxIdSql = "select id_seq_nextval('" + moduleName + "')";

		this.increment = sqlValue(getIncrementSql);
	}

	@Override
	public long getIncrement() {
		return increment;
	}

	@Override
	protected long nextMaxId() {
		return sqlValue(nextMaxIdSql);
	}

	private long sqlValue(String sql) {
		try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();) {
			try (ResultSet rs = st.executeQuery(sql)) {
				rs.next();
				return rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on sqlValue sql:" + sql, e);
		}
	}
}
