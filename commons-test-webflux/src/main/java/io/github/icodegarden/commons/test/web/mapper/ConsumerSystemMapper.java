package io.github.icodegarden.commons.test.web.mapper;

import io.github.icodegarden.commons.lang.query.BaseQuery;
import io.github.icodegarden.commons.mybatis.repository.MybatisRepository;
import io.github.icodegarden.commons.test.web.pojo.persistence.ConsumerSystemPO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ConsumerSystemMapper extends
		MybatisRepository<ConsumerSystemPO, ConsumerSystemPO.Update, BaseQuery, ConsumerSystemPO, ConsumerSystemPO> {
}
