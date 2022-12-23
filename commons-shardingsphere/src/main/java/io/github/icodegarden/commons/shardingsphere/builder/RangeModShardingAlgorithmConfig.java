package io.github.icodegarden.commons.shardingsphere.builder;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
@Setter
@ToString
public class RangeModShardingAlgorithmConfig {
	
	private String algorithmName;
	
	private List<Group> groups;

	public static class Group {
		private String name;
		private Long rangeGte;
		private Long rangeLt;
		/**
		 * 取模时的因子，例如?%mod
		 */
		private Integer mod;
		private String modLoadBalance;// {"ds0":[0,1],"ds1":[2],"ds2":[3,4]}
		/**
		 * 解析后
		 */
		private Map<String/*ds name 如ds0*/, List<Integer>/*mod 如[0,1]*/> mlb;

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

	public void validate() throws IllegalArgumentException {
		Assert.notEmpty(groups, "Missing:groups");
		for (Group group : groups) {
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