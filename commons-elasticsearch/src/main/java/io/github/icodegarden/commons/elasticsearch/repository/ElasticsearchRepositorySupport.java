package io.github.icodegarden.commons.elasticsearch.repository;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

import org.springframework.util.CollectionUtils;

import io.github.icodegarden.commons.elasticsearch.query.ElasticsearchQuery;
import io.github.icodegarden.commons.lang.Validateable;
import io.github.icodegarden.commons.lang.repository.Repository;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class ElasticsearchRepositorySupport<PO, U, Q extends ElasticsearchQuery<W>, W, DO>
		implements Repository<PO, U, Q, W, DO, String> {

	private final Class<PO> classPO;
	private final Class<U> classU;
	private final Class<DO> classDO;

	private final String index;

	/**
	 * 在source中的id名称
	 */
	private final String idFieldName;
	/**
	 * 多索引 000001 000002 ...
	 */
	private final boolean aliasOfMultiIndex;

	private int readTimeoutMillis = 3000;

	public ElasticsearchRepositorySupport(String index) {
		this(index, null);
	}

	public ElasticsearchRepositorySupport(String index, String idFieldName) {
		this(index, null, false);
	}

	public ElasticsearchRepositorySupport(String index, String idFieldName, boolean aliasOfMultiIndex) {
		if (aliasOfMultiIndex && idFieldName == null) {
			throw new IllegalArgumentException("idFieldName must not null when has aliasOfMultiIndex");
		}
		this.index = index;

		this.idFieldName = idFieldName;
		this.aliasOfMultiIndex = aliasOfMultiIndex;

		Type t1 = this.getClass().getGenericSuperclass();
//		if (t1 instanceof ParameterizedType) {
		ParameterizedType pt = (ParameterizedType) t1;
		Type[] actualTypeArguments = pt.getActualTypeArguments();

		classPO = (Class) actualTypeArguments[0];
		classU = (Class) actualTypeArguments[1];
		classDO = (Class) actualTypeArguments[4];
//		}
	}
	
	/**
	 * Foreach方式
	 */
	public int updateBatchForeach(Collection<U> updates) {
		if (CollectionUtils.isEmpty(updates)) {
			return 0;
		}
		for(U update:updates) {
			update(update);
		}
		return updates.size();
	}

	public void setReadTimeoutMillis(int readTimeoutMillis) {
		this.readTimeoutMillis = readTimeoutMillis;
	}

	public Class<PO> getClassPO() {
		return classPO;
	}

	public Class<U> getClassU() {
		return classU;
	}

	public Class<DO> getClassDO() {
		return classDO;
	}

	public String getIndex() {
		return index;
	}

	public String getIdFieldName() {
		return idFieldName;
	}

	public boolean isAliasOfMultiIndex() {
		return aliasOfMultiIndex;
	}

	public int getReadTimeoutMillis() {
		return readTimeoutMillis;
	}

	protected void validate(Object obj) {
		if (obj instanceof Validateable) {
			((Validateable) obj).validate();
		}
	}

	/**
	 * @return Nullable null表示不支持SearchAfter
	 */
	protected abstract String extractSearchAfter(DO obj);
}
