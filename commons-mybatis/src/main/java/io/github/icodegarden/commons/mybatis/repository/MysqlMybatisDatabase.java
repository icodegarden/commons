package io.github.icodegarden.commons.mybatis.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.Select;

import io.github.icodegarden.commons.lang.repository.OptimizeTableResults;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Mapper
public interface MysqlMybatisDatabase extends MybatisDatabase {

	@Select("<script> select version() as version </script>")
	@Override
	public String version();

	@Select("<script> show tables </script>")
	@Override
	public List<String> listTables();

	@Select("<script> select count(0) from ${tableName} </script>")
	@Override
	public long countTable(@Param("tableName") String tableName);

	@Select("<script> OPTIMIZE TABLE ${tableName} </script>")
	@ResultType(OptimizeTableResults.Result.class)
	@Override
	public OptimizeTableResults<OptimizeTableResults.Result> optimizeTable(@Param("tableName") String tableName);

}
