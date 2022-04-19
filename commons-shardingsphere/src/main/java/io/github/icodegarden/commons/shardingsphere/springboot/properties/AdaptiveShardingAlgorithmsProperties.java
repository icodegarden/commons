package io.github.icodegarden.commons.shardingsphere.springboot.properties;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * springboot模式的sharding，让配置同时进到这里
 * 需要纳入spring
 * 
 * @author Fangfang.Xu
 *
 */
@Configuration
@ConfigurationProperties(prefix = "spring.shardingsphere.rules.sharding.sharding-algorithms")
public class AdaptiveShardingAlgorithmsProperties {

	private Algorithm idrangemod = new Algorithm();

	@PostConstruct
	void init() {
		idrangemod.validate();
	}

	public Algorithm getIdrangemod() {
		return idrangemod;
	}

	public void setIdrangemod(Algorithm idrangemod) {
		this.idrangemod = idrangemod;
	}

	@Override
	public String toString() {
		return "ShardingAlgorithmsProperties [idrangemod=" + idrangemod + "]";
	}

	public static class Algorithm {
		private String type;
		private Props props;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Props getProps() {
			return props;
		}

		public void setProps(Props props) {
			this.props = props;
		}

		@Override
		public String toString() {
			return "Algorithm [type=" + type + ", props=" + props + "]";
		}

		public static class Props {
			private String strategy;
			private String algorithmClassName;
			private List<Group> groups;

			public String getStrategy() {
				return strategy;
			}

			public void setStrategy(String strategy) {
				this.strategy = strategy;
			}

			public String getAlgorithmClassName() {
				return algorithmClassName;
			}

			public void setAlgorithmClassName(String algorithmClassName) {
				this.algorithmClassName = algorithmClassName;
			}

			public List<Group> getGroups() {
				return groups;
			}

			public void setGroups(List<Group> groups) {
				this.groups = groups;
			}

			@Override
			public String toString() {
				return "Props [strategy=" + strategy + ", algorithmClassName=" + algorithmClassName + ", groups="
						+ groups + "]";
			}

			public static class Group {
				private String name;
				private Long rangeGte;
				private Long rangeLt;
				private Integer mod;
				private String modLoadBalance;// {"ds0":[0,1],"ds1":[2],"ds2":[3,4]}
				/**
				 * 解析后
				 */
				private Map<String, List<Integer>> mlb;

				public String getName() {
					return name;
				}

				public void setName(String name) {
					this.name = name;
				}

				public Long getRangeGte() {
					return rangeGte;
				}

				public void setRangeGte(Long rangeGte) {
					this.rangeGte = rangeGte;
				}

				public Long getRangeLt() {
					return rangeLt;
				}

				public void setRangeLt(Long rangeLt) {
					this.rangeLt = rangeLt;
				}

				public Integer getMod() {
					return mod;
				}

				public void setMod(Integer mod) {
					this.mod = mod;
				}

				public String getModLoadBalance() {
					return modLoadBalance;
				}

				public void setModLoadBalance(String modLoadBalance) {
					this.modLoadBalance = modLoadBalance;
				}

				public Map<String, List<Integer>> getMlb() {
					return mlb;
				}

				public void setMlb(Map<String, List<Integer>> mlb) {
					this.mlb = mlb;
				}

				@Override
				public String toString() {
					return "Group [name=" + name + ", rangeGte=" + rangeGte + ", rangeLt=" + rangeLt + ", mod=" + mod
							+ ", modLoadBalance=" + modLoadBalance + ", mlb=" + mlb + "]";
				}

			}
		}

		public void validate() throws IllegalArgumentException {
			Assert.hasText(type, "Missing:type");
			Assert.notNull(props, "Missing:props");
			Assert.hasText(props.getStrategy(), "Missing:props.strategy");
			Assert.hasText(props.getAlgorithmClassName(), "Missing:props.algorithmClassName");
			Assert.notNull(props.getGroups(), "Missing:props.groups");
			for (Props.Group group : props.getGroups()) {
				Assert.hasText(group.getName(), "Missing:group.name");
				Assert.notNull(group.getRangeGte(), "Missing:group.rangeGte");
				Assert.notNull(group.getRangeLt(), "Missing:group.rangeLt");
				Assert.notNull(group.getMod(), "Missing:group.mod");
				Assert.hasText(group.getModLoadBalance(), "Missing:group.modLoadBalance");
				Map<String, List<Integer>> loadBalance = JsonUtils.deserialize(group.getModLoadBalance(), Map.class);
				for (Entry<String, List<Integer>> entry : loadBalance.entrySet()) {
					Assert.hasText(entry.getKey(), "modLoadBalance missing json key");
					Assert.notEmpty(entry.getValue(), "modLoadBalance missing json value");
				}
			}
		}
	}

}