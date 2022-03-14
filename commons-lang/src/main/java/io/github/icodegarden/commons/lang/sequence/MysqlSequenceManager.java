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

	private volatile long max = -1;
	private AtomicLong current;

	private String moduleName;
	private DataSource dataSource;

	private final String currentIdSql;
	private final String nextMaxIdSql;

	public MysqlSequenceManager(String moduleName, DataSource dataSource) {
		Assert.hasLength(moduleName, "moduleName must not empty");
		Assert.notNull(dataSource, "dataSource must not null");
		this.moduleName = moduleName;
		this.dataSource = dataSource;

		currentIdSql = "select id_seq_currval('" + moduleName + "')";
		nextMaxIdSql = "select id_seq_nextval('" + moduleName + "')";
	}

	@Override
	public long currentId() {
		return current.get();
	}

	@Override
	public long nextId() {
		initIfNecessary();

		long id = current.incrementAndGet();
		updateMaxIfNecessary();

		return id;
	}

	private long currentIdInDb() {
		try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();) {
			try (ResultSet rs = st.executeQuery(currentIdSql)) {
				rs.next();
				return rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on get current id", e);
		}
	}

	private void initIfNecessary() {
		if (current == null) {
			synchronized (this) {
				if (current == null) {
					long currentId = currentIdInDb();
					current = new AtomicLong(currentId);

					updateMaxIfNecessary();
				}
			}
		}
	}

	private void updateMaxIfNecessary() {
		if (current.get() > max) {
			synchronized (this) {
				while (current.get() > max) {
					long nextMax = nextMaxIdInDb();
					max = nextMax;
				}
			}
		}
	}

	private long nextMaxIdInDb() {
		try (Connection conn = dataSource.getConnection(); Statement st = conn.createStatement();) {
			try (ResultSet rs = st.executeQuery(nextMaxIdSql)) {
				rs.next();
				return rs.getLong(1);
			}
		} catch (SQLException e) {
			throw new IllegalStateException("ex on get next max id", e);
		}
	}
}
