package io.github.icodegarden.commons.lang.concurrent.registry;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface DatabaseRegistryRepository<ID> {

	public static final String TABLE_NAME = "registry";

	/**
	 * 查询可能的已注册票据，不包含过期的
	 */
	SimpleRegistrationDO<ID> findByRegistration(Registration registration, String nowStr);

	/**
	 * 根据name查询任意可用的票据
	 */
	SimpleRegistrationDO<ID> findAnyAvailableByName(String name, String nowStr);

	/**
	 * 根据name查询最后的index
	 */
	SimpleRegistrationDO<ID> findMaxIndexByName(String name);

	void createOnRegister(int index, Registration registration, String nowStr);

	void updateOnRegister(ID id, Registration registration, String nowStr);

	void updateOnDeregister(ID id);

	/**
	 * 更新租期(keepalive)<br>
	 * 不可以用id作为条件
	 */
	int updateLease(Registration registration, String nowStr);

	/**
	 * 更新注册信息
	 */
	void updateRegistration(ID id, String metadata, String info);
	/**
	 * 查询可能的已注册票据，不包含过期的
	 */
	List<? extends Registration> findAllRegistered(String name, boolean withMetadata, boolean withInfo,String nowStr);

}
