package io.github.icodegarden.commons.shardingsphere.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import io.github.icodegarden.commons.shardingsphere.builder.RangeModShardingAlgorithmConfig;
import io.github.icodegarden.commons.shardingsphere.builder.RangeModShardingAlgorithmConfig.Group;

//# 分片算法配置      坑: <sharding-algorithm-name> 名字必须小写并且不能带 下划线
/**
 * 分组 先range 再mod， 根据mod负载均衡
 * 
 * @author Fangfang.Xu
 *
 */
public class RangeModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

	private static final Logger log = LoggerFactory.getLogger(FirstDataSourceShardingAlgorithm.class);

	public static final String ALGORITHM_NAME_KEY = "algorithmName";

	private Properties props = new Properties();

	private String algorithmName;

	private RangeModShardingAlgorithmConfig config;

	private static Map<String, RangeModShardingAlgorithmConfig> algorithmName_rangemod_map = new HashMap<>();

	public static void registerRangeModShardingAlgorithmConfig(String name, RangeModShardingAlgorithmConfig config) {
		algorithmName_rangemod_map.put(name, config);
	}

	@Override
	public void setProps(Properties props) {
		this.props = props;
	}

	@Override
	public void init() {
		Assert.notNull(props.get(ALGORITHM_NAME_KEY), ALGORITHM_NAME_KEY + " must not null");
		algorithmName = props.getProperty(ALGORITHM_NAME_KEY);

		prepareAlgorithmIfNecessary();
	}

	@Override
	public String doSharding(final Collection<String> availableTargetNames,
			final PreciseShardingValue<Comparable<?>> shardingValue) {
		long value = getLongValue(shardingValue.getValue());

		List<Group> groups = config.getGroups();
		for (Group group : groups) {
			if (value >= group.getRangeGte() && value < group.getRangeLt()) {
				if (log.isDebugEnabled()) {
					log.debug("value:{} is match group:{}", value, group);
				}
				int modResult = (int) value % group.getMod();

				Map<String, List<Integer>> loadBalance = group.getMlb();

				for (Entry<String, List<Integer>> entry : loadBalance.entrySet()) {
					if (entry.getValue().contains(modResult)) {
						if (log.isDebugEnabled()) {
							log.debug("value:{} in group:{} is loadBalance to:{}", value, group.getName(),
									entry.getKey());
						}
						return entry.getKey();
					}
				}
				if (log.isWarnEnabled()) {
					log.warn("datasource loadBalance not match which value:{} , group:{}", value, group);
				}
			}
		}

//		return null;
		throw new IllegalArgumentException("no target match for value:" + shardingValue.getValue());

//		return "";//应该表示的是路由到第一个datasource
	}

	private void prepareAlgorithmIfNecessary() {
		if (config == null) {
			synchronized (this) {
				if (config == null) {
					config = algorithmName_rangemod_map.get(algorithmName);

					Assert.notNull(config, String.format(
							"rangemod of algorithmName:%s not found, must registerRangeModShardingAlgorithmConfig() before",
							algorithmName));
					try {
						List<Group> groups = config.getGroups();
						for (Group group : groups) {
							String json = group.getModLoadBalance();
							group.setMlb(JsonUtils.deserialize(json, Map.class));
						}
					} catch (Exception e) {
						throw new IllegalStateException(e);
					}
				}
			}
		}
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
		return "RANGE-MOD";
	}

}