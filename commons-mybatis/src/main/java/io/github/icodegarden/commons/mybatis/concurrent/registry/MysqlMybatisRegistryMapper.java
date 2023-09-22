package io.github.icodegarden.commons.mybatis.concurrent.registry;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import io.github.icodegarden.commons.lang.concurrent.registry.DatabaseRegistryRepository;
import io.github.icodegarden.commons.lang.concurrent.registry.Registration;
import io.github.icodegarden.commons.lang.concurrent.registry.SimpleRegistrationDO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Mapper
public interface MysqlMybatisRegistryMapper extends DatabaseRegistryRepository<Long> {

	@Results(value = { //
			@Result(id = true, property = "id", column = "id", javaType = Long.class), //
			@Result(property = "index", column = "index") }//
	)
	@Select("<script> select id,`index` from " + DatabaseRegistryRepository.TABLE_NAME
			+ " where identifier = #{registration.identifier} and name = #{registration.name}  and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr} limit 1</script>")
	@Override
	SimpleRegistrationDO<Long> findByRegistration(@Param("registration") Registration registration,
			@Param("nowStr") String nowStr);

	@Results(value = { //
			@Result(id = true, property = "id", column = "id", javaType = Long.class), //
			@Result(property = "index", column = "index") }//
	)
	@Select("<script> select id,`index` from " + DatabaseRegistryRepository.TABLE_NAME
			+ " where name = #{name}  and (is_registered=0 OR DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) &lt; #{nowStr}) limit 1</script>")
	@Override
	SimpleRegistrationDO<Long> findAnyAvailableByName(@Param("name") String name, @Param("nowStr") String nowStr);

	@Results(value = { //
			@Result(id = true, property = "id", column = "id", javaType = Long.class), //
			@Result(property = "index", column = "index") }//
	)
	@Select("<script> select id,`index` from " + DatabaseRegistryRepository.TABLE_NAME
			+ " where name = #{name} order by `index` desc limit 1</script>")
	@Override
	SimpleRegistrationDO<Long> findMaxIndexByName(@Param("name") String name);

	@Insert("<script> insert into " + DatabaseRegistryRepository.TABLE_NAME
			+ " (`name`, `identifier`, `index`, `is_registered`, `metadata`, `info`, `expire_seconds`, `lease_at`)"
			+ " values(#{registration.name}, #{registration.identifier}, #{index}, 1, #{registration.metadata,jdbcType=OTHER,typeHandler=io.github.icodegarden.commons.mybatis.handler.JsonObjectHandler}, #{registration.info,jdbcType=OTHER,typeHandler=io.github.icodegarden.commons.mybatis.handler.JsonObjectHandler}, #{registration.expireSeconds}, #{nowStr})</script>")
	@Override
	void createOnRegister(@Param("index") int index, @Param("registration") Registration registration,
			@Param("nowStr") String nowStr);

	@Update("<script> update " + DatabaseRegistryRepository.TABLE_NAME
			+ " set name=#{registration.name},identifier=#{registration.identifier},is_registered=1,expire_seconds=#{registration.expireSeconds},lease_at=#{nowStr}"
			+ " where id=#{id}</script>")
	@Override
	void updateOnRegister(@Param("id") Long id, @Param("registration") Registration registration,
			@Param("nowStr") String nowStr);

	@Update("<script> update " + DatabaseRegistryRepository.TABLE_NAME + " set is_registered=0"
			+ " where id=#{id}</script>")
	@Override
	void updateOnDeregister(@Param("id") Long id);

	@Update("<script> update " + DatabaseRegistryRepository.TABLE_NAME + " set lease_at=#{nowStr}"
			+ " where identifier = #{registration.identifier} and name = #{registration.name} and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr}</script>")
	@Override
	int updateLease(@Param("registration") Registration registration, @Param("nowStr") String nowStr);

	@Update("<script> update " + DatabaseRegistryRepository.TABLE_NAME + " set metadata=#{metadata},info=#{info}"
			+ " where id=#{id}</script>")
	@Override
	void updateRegistration(@Param("id") Long id, @Param("metadata") String metadata, @Param("info") String info);

	@ResultType(value = Registration.Default.class)
	@SelectProvider(type = SqlProvider.class, method = "findAllRegistered")
	@Override
	List<Registration.Default> findAllRegistered(@Param("name") String name, @Param("withMetadata") boolean withMetadata,
			@Param("withInfo") boolean withInfo, @Param("nowStr") String nowStr);

	class SqlProvider {
		public String findAllRegistered(@Param("name") String name, @Param("withMetadata") boolean withMetadata,
				@Param("withInfo") boolean withInfo, @Param("nowStr") String nowStr) {
			StringBuilder sb = new StringBuilder(200)//
//					.append("select id,identifier,`index`,expire_seconds");
					.append("select name,identifier,expire_seconds");
			if (withMetadata) {
				sb.append(",metadata");
			}
			if (withInfo) {
				sb.append(",info");
			}

			String sql = sb.append(" from ").append(TABLE_NAME)//
					/*
					 * 要求is_registered=1 并且 没有过期
					 */
//					.append(" where name = #{name} and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) &gt;= #{nowStr}")//
					/**
					 * 这里不能识别&gt;=
					 */
					.append(" where name = #{name} and is_registered=1 and DATE_ADD(lease_at,INTERVAL expire_seconds SECOND) >= #{nowStr}")//
					.toString();
			return sql;
		}
	}
}
