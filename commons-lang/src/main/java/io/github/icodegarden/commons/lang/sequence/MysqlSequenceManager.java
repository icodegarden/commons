package io.github.icodegarden.commons.lang.sequence;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.springframework.util.Assert;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlSequenceManager implements SequenceManager {

	private String moduleName;
	private AtomicLong localCurrent = new AtomicLong(-1);
	private AtomicLong localMax = new AtomicLong(-1);
	private final long increment;

	private DataSource dataSource;

	private final String getIncrementSql;
//	private final String currentIdSql;
	private final String nextMaxIdSql;

	public MysqlSequenceManager(String moduleName, DataSource dataSource) {
		Assert.hasLength(moduleName, "moduleName must not empty");
		Assert.notNull(dataSource, "dataSource must not null");
		this.moduleName = moduleName;
		this.dataSource = dataSource;

		getIncrementSql = "select `increment` from `id_sequence` where name = ('" + moduleName + "')";
//		currentIdSql = "select id_seq_currval('" + moduleName + "')";
		nextMaxIdSql = "select id_seq_nextval('" + moduleName + "')";

		this.increment = sqlValue(getIncrementSql);
	}

	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @return 步长
	 */
	public long getIncrement() {
		return increment;
	}

	@Override
	public long currentId() {
		return localCurrent.get();
	}

	/**
	 * 如果本地当前值>=本地最大值<br>
	 * 首次 取next最大值 起点=最大值-步长 最大值=next最大值<br>
	 * 后续 取next最大值 起点=最大值-步长 最大值=next最大值<br>
	 * 
	 */
	@Override
	public long nextId() {
		if (localCurrent.get() >= localMax.get()) {
			synchronized (this) {
				if (localCurrent.get() >= localMax.get()) {
					long nextMaxIdInDb = sqlValue(nextMaxIdSql);
					localCurrent.set(nextMaxIdInDb - increment);
					localMax.set(nextMaxIdInDb);
				}
			}
		}
		return localCurrent.incrementAndGet();
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
