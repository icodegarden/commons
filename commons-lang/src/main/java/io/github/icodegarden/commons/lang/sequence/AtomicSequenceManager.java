package io.github.icodegarden.commons.lang.sequence;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.util.Assert;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class AtomicSequenceManager implements SequenceManager {

	private String moduleName;
	private AtomicLong localCurrent = new AtomicLong(-1);
	private AtomicLong localMax = new AtomicLong(-1);

	public AtomicSequenceManager(String moduleName) {
		Assert.hasLength(moduleName, "moduleName must not empty");
		this.moduleName = moduleName;
	}

	public String getModuleName() {
		return moduleName;
	}

	/**
	 * @return 步长
	 */
	public abstract long getIncrement();

	@Override
	public long currentId() {
		return localCurrent.get();
	}

	/**
	 * 如果本地当前值>=本地最大值<br>
	 * 首次 取next最大值 起点=最大值-步长 最大值=next最大值<br>
	 * 后续 取next最大值 起点=最大值-步长 最大值=next最大值<br>
	 * 
	 */
	@Override
	public long nextId() {
		if (getIncrement() == 1) {
			long v = nextMaxId();
			localCurrent.set(v);
			return v;
		}
		synchronized (this) {
			if (localCurrent.get() >= localMax.get()) {
				long nextMaxIdInDb = nextMaxId();
				localCurrent.set(nextMaxIdInDb - getIncrement());
				localMax.set(nextMaxIdInDb);
			}
			return localCurrent.incrementAndGet();
		}
	}

	/**
	 * 原子递增 
	 */
	protected abstract long nextMaxId();
}
