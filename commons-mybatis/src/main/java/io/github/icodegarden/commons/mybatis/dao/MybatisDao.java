package io.github.icodegarden.commons.mybatis.dao;
import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.commons.lang.dao.Dao;
import io.github.icodegarden.commons.lang.query.BaseQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MybatisDao<PO, U, Q extends BaseQuery, W, DO> extends Dao<PO, U, Q, W, DO, Object> {

	DO findOne(@Param("id") Object id, @Param("with") W with);

}
