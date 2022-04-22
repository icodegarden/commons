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
import io.github.icodegarden.commons.shardingsphere.properties.Rangemod;
import io.github.icodegarden.commons.shardingsphere.properties.Rangemod.Group;

//# 分片算法配置      坑: <sharding-algorithm-name> 名字必须小写并且不能带 下划线
/**
 * 
 * @author Fangfang.Xu
 *
 */
public class RangeModShardingAlgorithm implements StandardShardingAlgorithm<Comparable<?>> {

	private static final Logger log = LoggerFactory.getLogger(FirstDataSourceShardingAlgorithm.class);

	private static final String NAME_KEY = "name";

	private Properties props = new Properties();

	private String name;

	@Override
	public void setProps(Properties props) {
		this.props = props;
	}

	@Override
	public void init() {
		Assert.notNull(props.get(NAME_KEY), NAME_KEY + " must not null");
		name = props.getProperty(NAME_KEY);
	}

	private static Map<String, Rangemod> name_rangemod_map = new HashMap<>();

	public static void registerRangemod(String name, Rangemod rangemod) {
		name_rangemod_map.put(name, rangemod);
	}

	private Rangemod rangemod;

	@Override
	public String doSharding(final Collection<String> availableTargetNames,
			final PreciseShardingValue<Comparable<?>> shardingValue) {
		prepareAlgorithmIfNecessary();

		long value = getLongValue(shardingValue.getValue());

		List<Group> groups = rangemod.getGroups();
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
		if (rangemod == null) {
			synchronized (this) {
				if (rangemod == null) {
					rangemod = name_rangemod_map.get(name);
					if (rangemod == null) {
						throw new IllegalAccessError(String.format("rangemod of name:%s not found"));
					}
					try {
						List<Group> groups = rangemod.getGroups();
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