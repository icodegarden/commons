package io.github.icodegarden.commons.shardingsphere.algorithm;

import java.util.Collection;
import java.util.Properties;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;

/**
 * 始终路由到第一个ds
 * 
 * @author Fangfang.Xu
 *
 */
public class FirstDataSourceShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

	private static final Logger log = LoggerFactory.getLogger(FirstDataSourceShardingAlgorithm.class);

	@Override
	public void setProps(Properties props) {

	}

	@Override
	public void init() {
	}

	private static ShardingSphereDataSource staticShardingSphereDataSource;

	public static void registerDataSource(ShardingSphereDataSource shardingSphereDataSource) {
		staticShardingSphereDataSource = shardingSphereDataSource;
	}

	@Override
	public String doSharding(final Collection<String> availableTargetNames,
			final PreciseShardingValue<Comparable<?>> shardingValue) {
		Assert.notNull(staticShardingSphereDataSource, "Missing:staticShardingSphereDataSource");
		String name = DataSourceUtils.firstDataSourceName(staticShardingSphereDataSource);
		if (log.isDebugEnabled()) {
			log.debug("first data source name:{}", name);
		}
		return name;
	}

	@Override
	public Collection<String> doSharding(final Collection<String> availableTargetNames,
			final RangeShardingValue<Comparable<?>> shardingValue) {
		throw new UnsupportedOperationException("Not Support for RangeShardingValue");
	}

	@Override
	public String getType() {
		return "FIRST-DATASOURCE";
	}
}