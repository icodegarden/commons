package io.github.icodegarden.commons.mybatis.dao;

import org.apache.ibatis.annotations.Param;

import io.github.icodegarden.commons.mybatis.dao.MybatisDao;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DemoMapper extends
		MybatisDao<DemoPO, DemoPO.Update, DemoQuery, DemoQuery.With, DemoDO> {

//	void add(ConsumerSystemPO po);

//	List<ConsumerSystemDO> findAll(ConsumerSystemQuery query);

	/**
	 * 
	 * @param id   NotNull
	 * @param with Nullable
	 * @return
	 */
//	ConsumerSystemDO findOne(@Param("id") Long id, @Param("with") ConsumerSystemQuery.With with);

	DemoDO findOneByAppId(@Param("appId") String appId, @Param("with") DemoQuery.With with);

//	int update(ConsumerSystemPO.Update update);

//	void delete(@Param("id") Long id);
}