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
	 * 查询可能的已注册票据
	 */
	SimpleDO<ID> findByRegistration(Registration registration, String nowStr);

	/**
	 * 根据name查询任意可用的票据
	 */
	SimpleDO<ID> findAnyAvailableByName(String name, String nowStr);

	/**
	 * 根据name查询最后的index
	 */
	SimpleDO<ID> findLastByName(String name);

	void createOnRegister(int index, Registration registration);

	void updateOnRegister(ID id, Registration registration);

	void updateOnDeregister(Registration registration);

	/**
	 * 更新租期(keepalive)
	 */
	boolean updateLease(Registration registration);

	/**
	 * 更新注册信息
	 */
	void updateRegistration(ID id, Registration registration);

	List<Registration> findAllRegistered(String name);

}
