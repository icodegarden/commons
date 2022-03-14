package io.github.icodegarden.commons.lang.metrics;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.github.icodegarden.commons.lang.NamedObject;
import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class Metrics implements NamedObject, Comparable<Metrics>, Serializable {
	private static final long serialVersionUID = 1L;

	private String serviceName;
	private String instanceName;
	private Map<DimensionName, Dimension> dimensions = new HashMap<DimensionName, Dimension>(4, 1);
	private String desc;

	/**
	 * kryo序列化
	 */
	Metrics() {
	}

	/**
	 * 至少有1个Dimension入参
	 * 
	 * @param d          NotNull
	 * @param dimensions
	 */
	public Metrics(Dimension d, Dimension... dimensions) {
		if (d == null) {
			throw new IllegalArgumentException("param notnull must not null");
		}
		setDimension(d);
		for (Dimension dimension : dimensions) {
			setDimension(dimension);
		}
	}

	/**
	 * 
	 * @param dimensions NotEmpty
	 */
	public Metrics(List<Dimension> dimensions) {
		if (dimensions == null || dimensions.isEmpty()) {
			throw new IllegalArgumentException("param dimensions must not empty");
		}
		dimensions.forEach(dimension -> {
			setDimension(dimension);
		});
	}

	@Override
	public String name() {
		return getServiceName();
	}

	public void setDimension(Dimension dimension) {
		dimensions.put(dimension.getDimensionName(), dimension);
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Map<DimensionName, Dimension> getDimensions() {
		return dimensions;
	}

	public Dimension getDimension(DimensionName dimensionName) {
		return dimensions.get(dimensionName);
	}

	/**
	 * 线程安全
	 * 
	 * @param dimensionName
	 * @param value
	 * @return 是否确实存在该维度，存在则会变动即使used超过max
	 */
	public boolean incrementDimension(DimensionName dimensionName, double value) {
		Dimension dimension = getDimension(dimensionName);
		if (dimension != null) {
			synchronized (dimension) {
				dimension.setUsed(dimension.getUsed() + value);
			}
			return true;
		}
		return false;
	}

	/**
	 * 线程安全
	 * 
	 * @param dimensionName
	 * @param value
	 * @return 是否确实存在该维度，存在则会变动
	 */
	public boolean decrementDimension(DimensionName dimensionName, double value) {
		Dimension dimension = getDimension(dimensionName);
		if (dimension != null) {
			synchronized (dimension) {
				dimension.setUsed(dimension.getUsed() - value);
				if (dimension.getUsed() < 0) {
					dimension.setUsed(0);
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 刷新cpu、memory已使用的值
	 */
	public void refreshUsedValues() {
		Dimension cpuD = getDimension(DimensionName.Cpu);
		if (cpuD != null) {
			cpuD.setUsed(SystemUtils.getVmRuntime().getProcessCpuLoad());
		}
		Dimension memoryD = getDimension(DimensionName.Memory);
		if (memoryD != null) {
			memoryD.setUsed(SystemUtils.getVmRuntime().getJvmUsedMemory() / 1024 / 1024);
		}
	}

	/**
	 * @return 只要有一项指标使用率达到最大值，则超载
	 */
	public boolean isOverload() {
		for (Dimension dimension : dimensions.values()) {
			if (dimension.getUsed() >= dimension.getMax()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 超过负载的维度
	 * 
	 * @return
	 */
	public List<Dimension> overloadDimensions() {
		return dimensions.values().stream().filter(dimension -> dimension.getUsed() >= dimension.getMax())
				.collect(Collectors.toList());
	}

	/**
	 * 只计算instanceName
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((instanceName == null) ? 0 : instanceName.hashCode());
		return result;
	}

	/**
	 * 只比较instanceName
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metrics other = (Metrics) obj;
		if (instanceName == null) {
			if (other.instanceName != null)
				return false;
		} else if (!instanceName.equals(other.instanceName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[instanceName=" + instanceName + ", dimensions=" + dimensions + "]";
	}

	public static class DimensionName implements Serializable {
		private static final long serialVersionUID = 6468102993100427298L;

		public static final DimensionName Cpu = new DimensionName("cpu");
		public static final DimensionName Memory = new DimensionName("memory");
		/**
		 * 任务负载能力<br>
		 * max=job个数*weight*每秒执行频率 = 10000*1*每秒执行频率<br>
		 * max由用户决定，used=累加 每个任务的weight*每秒执行频率<br>
		 * 例如10000可以是： 10000个*重量1*1000ms/频率1000ms , 1000个*重量1*1000ms/频率100ms ,
		 * 2500个*2重量*1000ms/频率500ms , 20000个*5重量*1000ms/频率10000ms<br>
		 */
		public static final DimensionName Jobs = new DimensionName("jobs");

		private final String value;

		/**
		 * kryo序列化
		 */
		DimensionName() {
			value = null;
		}

		public DimensionName(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DimensionName other = (DimensionName) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DimensionName [value=" + value + "]";
		}
	}

	public static class Dimension implements Serializable {
		private static final long serialVersionUID = 1L;

		private DimensionName dimensionName;
		private double max;
		private double used;
		private int weight;
		private String desc;

		/**
		 * kryo序列化
		 */
		Dimension() {
		}

		/**
		 * weight使用默认值1
		 * 
		 * @param dimensionName
		 * @param max
		 * @param used
		 */
		public Dimension(DimensionName dimensionName, double max, double used) {
			this(dimensionName, max, used, 1);
		}

		/**
		 * 
		 * @param dimensionName
		 * @param max
		 * @param used
		 * @param weight        范围必须1-100
		 */
		public Dimension(DimensionName dimensionName, double max, double used, int weight) {
			if (weight < 0 || weight > 100) {
				throw new IllegalArgumentException("weight must range [0-100]");
			}
			this.dimensionName = dimensionName;
			this.max = max;
			this.used = used;
			this.weight = weight;
		}

		public DimensionName getDimensionName() {
			return dimensionName;
		}

		public void setDimensionName(DimensionName dimensionName) {
			this.dimensionName = dimensionName;
		}

		public double getMax() {
			return max;
		}

		public void setMax(double max) {
			this.max = max;
		}

		public double getUsed() {
			return used;
		}

		public void setUsed(double used) {
			this.used = used;
		}

		public int getWeight() {
			return weight;
		}

		public void setWeight(int weight) {
			this.weight = weight;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		@Override
		public String toString() {
			return "Dimension [dimensionName=" + dimensionName + ", max=" + max + ", used=" + used + ", weight="
					+ weight + ", desc=" + desc + "]";
		}

	}

	/**
	 * other == pre
	 */
	@Override
	public int compareTo(Metrics other) {
		if (other == null) {
			return -1;
		}
		Map<DimensionName, Dimension> thisDimensions = this.getDimensions();
		Map<DimensionName, Dimension> otherDimensions = other.getDimensions();

		/**
		 * this的剩余率-other的剩余率
		 */
		AtomicReference<Double> minus = new AtomicReference<Double>(0D);

		thisDimensions.forEach((thisName, thisDimension) -> {
			Dimension otherDimension = otherDimensions.get(thisName);
			// 剩余率比较
			double thisRemainRate = 0;
			double otherRemainRate = 0;
			if (otherDimension == null) {
				thisRemainRate = (thisDimension.getMax() - thisDimension.getUsed()) / thisDimension.getMax();
				thisRemainRate *= thisDimension.getWeight();
			} else {
				thisRemainRate = (thisDimension.getMax() - thisDimension.getUsed()) / thisDimension.getMax();
				thisRemainRate *= thisDimension.getWeight();

				otherRemainRate = (otherDimension.getMax() - otherDimension.getUsed()) / otherDimension.getMax();
				otherRemainRate *= otherDimension.getWeight();
			}
			minus.set(minus.get() + thisRemainRate - otherRemainRate);
		});
		if (minus.get() == 0) {
			return 0;
		}
		// 如果是正的，this排前面
		return minus.get() > 0 ? -1 : 1;
	}
}
