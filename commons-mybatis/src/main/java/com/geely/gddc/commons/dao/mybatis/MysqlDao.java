package com.geely.gddc.commons.dao.mybatis;
import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.commons.lang.dao.Dao;
import io.github.icodegarden.commons.lang.query.BaseQuery;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MysqlDao<PO, U, Q extends BaseQuery, W, DO> extends Dao<PO, U, Q, W, DO, Long> {

	DO findOne(@Param("id") Long id, @Param("with") W with);

}
