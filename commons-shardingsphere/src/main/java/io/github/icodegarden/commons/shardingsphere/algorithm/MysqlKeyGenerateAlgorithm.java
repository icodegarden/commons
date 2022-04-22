package io.github.icodegarden.commons.shardingsphere.algorithm;

import java.util.Properties;

import javax.sql.DataSource;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.sharding.spi.KeyGenerateAlgorithm;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.sequence.MysqlSequenceManager;
import io.github.icodegarden.commons.lang.sequence.SequenceManager;
import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class MysqlKeyGenerateAlgorithm implements KeyGenerateAlgorithm {

	private static final String MODULE_NAME_KEY = "moduleName";

	private Properties props = new Properties();

	private String moduleName;

	private volatile SequenceManager sequenceManager;

	@Override
	public void setProps(Properties props) {
		this.props = props;
	}

	@Override
	public void init() {
		moduleName = props.getProperty(MODULE_NAME_KEY);
		Assert.hasLength(moduleName, MODULE_NAME_KEY + " must not empty");
	}

	private static DataSource staticDataSource;

	public static void registerDataSource(DataSource dataSource) {
		staticDataSource = dataSource;
	}

	@Override
	public Comparable<?> generateKey() {
		if (sequenceManager == null) {
			Assert.notNull(staticDataSource, "Missing:staticDataSource");

			synchronized (this) {
				if (sequenceManager == null) {
					if (staticDataSource instanceof ShardingSphereDataSource) {
						DataSource dataSource = DataSourceUtils
								.firstDataSource((ShardingSphereDataSource) staticDataSource);
						sequenceManager = new MysqlSequenceManager(moduleName, dataSource);
					} else {
						sequenceManager = new MysqlSequenceManager(moduleName, staticDataSource);
					}
				}
			}
		}
		return sequenceManager.nextId();
	}

	@Override
	public String getType() {
		return "MYSQL";
	}
}
