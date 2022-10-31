package com.geely.gddc.commons.dao.mybatis;

import org.apache.ibatis.annotations.Param;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DemoMapper extends
		MysqlDao<DemoPO, DemoPO.Update, DemoQuery, DemoQuery.With, DemoDO> {

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