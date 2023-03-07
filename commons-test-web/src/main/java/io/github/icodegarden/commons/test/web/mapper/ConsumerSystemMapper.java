package io.github.icodegarden.commons.test.web.mapper;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.mybatis.dao.MybatisDao;
import io.github.icodegarden.commons.test.web.pojo.persistence.ConsumerSystemPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ConsumerSystemMapper
		extends MybatisDao<ConsumerSystemPO, ConsumerSystemPO.Update, BaseQuery, Object, ConsumerSystemPO> {

	ConsumerSystemPO findOneByAppId(@Param("appId") String appId, @Param("with") Object with);
}
