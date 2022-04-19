package io.github.icodegarden.commons.shardingsphere.springboot;

import java.util.Collection;
import java.util.Properties;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.shardingsphere.util.DataSourceUtils;
import io.github.icodegarden.commons.springboot.SpringContext;

/**
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

	@Override
	public String doSharding(final Collection<String> availableTargetNames,
			final PreciseShardingValue<Comparable<?>> shardingValue) {
		ShardingSphereDataSource dataSource = SpringContext.getApplicationContext()
				.getBean(ShardingSphereDataSource.class);
		String name = DataSourceUtils.firstDataSourceName(dataSource);
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

	private long getLongValue(final Comparable<?> value) {
		return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
	}

	@Override
	public String getType() {
		return "FIRST-DATASOURCE";
	}
}