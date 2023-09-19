package io.github.icodegarden.commons.lang.dao;

import java.util.Collection;
import java.util.List;

import io.github.icodegarden.commons.lang.query.BaseQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Dao<PO, U, Q extends BaseQuery, W, DO, ID> {

	void add(PO po);

	default void addBatch(Collection<PO> pos) {
		pos.forEach(this::add);
	}

	int update(U update);
	
	int updateBatch(Collection<U> updates);

	List<DO> findAll(Q query);

	DO findOne(ID id, W with);

	List<DO> findByIds(List<ID> ids, W with);

	long count(Q query);

	int delete(ID id);

	int deleteBatch(Collection<ID> ids);
	
	int deleteByQuery(Q query);
}