package io.github.icodegarden.commons.shardingsphere.springboot;
//package io.github.icodegarden.beecomb.master.shardingsphere;
//
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Properties;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
//import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;
//import org.springframework.util.Assert;
//
//import io.github.icodegarden.commons.lang.util.JsonUtils;
//import lombok.Getter;
//import lombok.ToString;
//import lombok.extern.slf4j.Slf4j;
//
////# 分片算法配置      坑: <sharding-algorithm-name> 名字必须小写并且不能带 下划线
////spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.type=CLASS_BASED
////spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.strategy=standard
////spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.algorithmClassName=io.github.icodegarden.beecomb.master.shardingsphere.RangeModShardingAlgorithm
////spring.shardingsphere.rules.sharding.sharding-algorithms.idrangemod.props.groups=[{"name":"group0","rangeGte":0,"rangeLt":200000,"mod":2,"modLoadBalance":{"ds0":[0],"ds1":[1]}}]
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//@Slf4j
//public class RangeModShardingAlgorithmBak implements StandardShardingAlgorithm<Comparable<?>> {
//
//	private static final String GROUPS_KEY = "groups";
//
////	private static final String RANGEGTE_KEY = "rangeGte";
////	private static final String RANGELT_KEY = "rangeLt";
////	private static final String MOD_KEY = "mod";
//	private static final String MOD_LOAD_BALANCE_KEY = "modLoadBalance";
//
//	private List<Group> groups;
//
//	private Properties props = new Properties();
//
//	@Override
//	public void setProps(Properties props) {
//		this.props = props;
//	}
//
//	@Override
//	public void init() {
//		Assert.notNull(props.get(GROUPS_KEY), GROUPS_KEY + " must not null");
//
//		String groupsStr = props.getProperty(GROUPS_KEY);
//
////		Map<String/* name */, Map<String, Object>> groups = JsonUtils.deserialize(groupsStr, Map.class);
////
////		this.groups = groups.entrySet().stream().map(g -> {
////			String name = g.getKey();
////			Map<String, Object> value = g.getValue();
////
////			Long rangeGte = (Long) value.get(RANGEGTE_KEY);
////			Long rangeLt = (Long) value.get(RANGELT_KEY);
////			Integer mod = (Integer) value.get(MOD_KEY);
////			Map<String/* ds */, List<Integer>> modLoadBalance = (Map) value.get(MOD_LOAD_BALANCE_KEY);
////
////			Group group = new Group(name, rangeGte, rangeLt, mod, modLoadBalance);
////			return group;
////		}).collect(Collectors.toList());
//
//		this.groups = JsonUtils.deserializeArray(groupsStr, Group.class);
//		this.groups.forEach(group -> group.validate());
//
//		if (log.isInfoEnabled()) {
//			log.info("prepared groups:{}", this.groups);
//		}
//	}
//
//	@Override
//	public String doSharding(final Collection<String> availableTargetNames,
//			final PreciseShardingValue<Comparable<?>> shardingValue) {
//		long value = getLongValue(shardingValue.getValue());
//
//		for (Group group : groups) {
//			if (value >= group.getRangeGte() && value < group.getRangeLt()) {
//				if (log.isDebugEnabled()) {
//					log.debug("value:{} is match group:{}", value, group);
//				}
//				int modResult = (int) value % group.getMod();
//				Map<String, List<Integer>> loadBalance = group.getModLoadBalance();
//				for (Entry<String, List<Integer>> entry : loadBalance.entrySet()) {
//					if (entry.getValue().contains(modResult)) {
//						if (log.isDebugEnabled()) {
//							log.debug("value:{} in group:{} is loadBalance to:{}", value, group.getName(),
//									entry.getKey());
//						}
//						return entry.getKey();
//					}
//				}
//				if (log.isWarnEnabled()) {
//					log.warn("datasource loadBalance not match which value:{} , group:{}", value, group);
//				}
//			}
//		}
//
////		return null;
//		throw new IllegalArgumentException("no target match for value:" + shardingValue.getValue());
//
////		return "";//应该表示的是路由到第一个datasource
//	}
//
//	@Override
//	public Collection<String> doSharding(final Collection<String> availableTargetNames,
//			final RangeShardingValue<Comparable<?>> shardingValue) {
//		throw new UnsupportedOperationException("Not Support for RangeShardingValue");
//	}
//
//	private long getLongValue(final Comparable<?> value) {
//		return value instanceof Number ? ((Number) value).longValue() : Long.parseLong(value.toString());
//	}
//
//	@Override
//	public String getType() {
//		return "RANGE-MOD";
//	}
//
//	@Getter
//	@ToString
//	public static class Group {
//		private String name;
//		private Long rangeGte;
//		private Long rangeLt;
//		private Integer mod;
//		private Map<String/* ds */, List<Integer>/* mod result */> modLoadBalance;
//
//		public Group() {
//		}
//
////		public Group(String name, Long rangeGte, Long rangeLt, Integer mod, Map<String, List<Integer>> modLoadBalance) {
////			Assert.hasLength(name, "name must not empty");
////			Assert.notNull(rangeGte, "rangeGte must not null, name:" + name);
////			Assert.notNull(rangeLt, "rangeLt must not null, name:" + name);
////			Assert.notNull(mod, "mod must not null, name:" + name);
////			Assert.notEmpty(modLoadBalance, "modLoadBalance must not empty, name:" + name);
////
////			Set<Integer> mods = modLoadBalance.values().stream().flatMap(list -> list.stream())
////					.collect(Collectors.toSet());
////			Assert.isTrue(mods.size() == mod,
////					String.format("size in %s:%d must eq mod:%s, ", MOD_LOAD_BALANCE_KEY, mods.size(), mod));
////
////			this.name = name;
////			this.rangeGte = rangeGte;
////			this.rangeLt = rangeLt;
////			this.mod = mod;
////			this.modLoadBalance = modLoadBalance;
////		}
//
//		public void validate() {
//			Assert.hasLength(name, "group name must not empty");
//			Assert.notNull(rangeGte, "rangeGte must not null, name:" + name);
//			Assert.notNull(rangeLt, "rangeLt must not null, name:" + name);
//			Assert.notNull(mod, "mod must not null, name:" + name);
//			Assert.notEmpty(modLoadBalance, "modLoadBalance must not empty, name:" + name);
//
//			Set<Integer> mods = modLoadBalance.values().stream().flatMap(list -> list.stream())
//					.collect(Collectors.toSet());
//			Assert.isTrue(mods.size() == mod, String.format("size in %s:%d must eq mod:%s where name:%s ",
//					MOD_LOAD_BALANCE_KEY, mods.size(), mod, name));
//		}
//	}
//}