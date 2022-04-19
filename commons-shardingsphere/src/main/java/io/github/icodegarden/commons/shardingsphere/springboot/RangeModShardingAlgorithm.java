package io.github.icodegarden.commons.shardingsphere.springboot;

import java.lang.reflect.Field;
import java.util.Collection;
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
import io.github.icodegarden.commons.shardingsphere.springboot.properties.AdaptiveShardingAlgorithmsProperties;
import io.github.icodegarden.commons.shardingsphere.springboot.properties.AdaptiveShardingAlgorithmsProperties.Algorithm;
import io.github.icodegarden.commons.shardingsphere.springboot.properties.AdaptiveShardingAlgorithmsProperties.Algorithm.Props;
import io.github.icodegarden.commons.springboot.SpringContext;

//# 分片算法配置      坑: <sharding-algorithm-name> 名字必须小写并且不能带 下划线
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.type=CLASS_BASED
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.strategy=standard
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.algorithmClassName=io.github.icodegarden.beecomb.master.shardingsphere.RangeModShardingAlgorithm
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.name=idrangemod
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].name=group0
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeGte=0
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].rangeLt=200000
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].mod=2
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[0].modLoadBalance={"ds0":[0],"ds1":[1]}
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].name=group1
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].rangeGte=200000
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].rangeLt=400000
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].mod=3
//spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups[1].modLoadBalance={"ds3":[0,1],"ds4":[2]}
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

	private Algorithm algorithm;

	@Override
	public String doSharding(final Collection<String> availableTargetNames,
			final PreciseShardingValue<Comparable<?>> shardingValue) {
		prepareAlgorithmIfNecessary();

		long value = getLongValue(shardingValue.getValue());

		Props props = algorithm.getProps();
		List<AdaptiveShardingAlgorithmsProperties.Algorithm.Props.Group> groups = props.getGroups();
		for (AdaptiveShardingAlgorithmsProperties.Algorithm.Props.Group group : groups) {
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
		try {
			if (algorithm == null) {
				synchronized (this) {
					if (algorithm == null) {
						AdaptiveShardingAlgorithmsProperties properties = SpringContext.getApplicationContext()
								.getBean(AdaptiveShardingAlgorithmsProperties.class);

						Field field = AdaptiveShardingAlgorithmsProperties.class.getDeclaredField(name);
						boolean accessible = field.isAccessible();
						field.setAccessible(true);
						algorithm = (Algorithm) field.get(properties);
						field.setAccessible(accessible);

						Props props = algorithm.getProps();
						List<AdaptiveShardingAlgorithmsProperties.Algorithm.Props.Group> groups = props.getGroups();

						for (AdaptiveShardingAlgorithmsProperties.Algorithm.Props.Group group : groups) {
							String json = group.getModLoadBalance();
							group.setMlb(JsonUtils.deserialize(json, Map.class));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
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