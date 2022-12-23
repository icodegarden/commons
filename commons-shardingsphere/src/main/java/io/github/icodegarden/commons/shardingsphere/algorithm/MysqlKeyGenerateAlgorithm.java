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

	public static final String TYPE = "MYSQL";
	public static final String MODULE_NAME_KEY = "moduleName";

	private Properties props = new Properties();

	private String moduleName;

	private volatile SequenceManager sequenceManager;

	private static DataSource staticDataSource;

	public static void registerDataSource(DataSource dataSource) {
		staticDataSource = dataSource;
	}
	
	/**
	 * 这个东西他做的不好，不能使用继承方式，只能得到String property，因为触发这个方法之前他会new一个Properties把所有props以string方式copy进去到这里
	 */
	@Override
	public void setProps(Properties props) {
		Assert.notNull(staticDataSource, "must call registerDataSource() before");
		this.props = props;
	}

	@Override
	public void init() {
		moduleName = props.getProperty(MODULE_NAME_KEY);
		Assert.hasLength(moduleName, MODULE_NAME_KEY + " must not empty");
		
		if (sequenceManager == null) {
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
	}

	@Override
	public Comparable<?> generateKey() {
		return sequenceManager.nextId();
	}

	@Override
	public String getType() {
		return TYPE;
	}
}
