package io.github.icodegarden.commons.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * <result property="content" column="content" 
			javaType="com.geely.ros.common.po.FunctionAssignmentPO$Content"
			typeHandler="io.github.icodegarden.commons.mybatis.handler.JsonObjectHandler"/>
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
public class JsonObjectHandler<T extends Object> extends BaseTypeHandler<T> {
	private Class<T> clazz;

	public JsonObjectHandler(Class<T> clazz) {
		Objects.requireNonNull(clazz, "Type argument cannot be null");
		this.clazz = clazz;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, T parameter, JdbcType jdbcType) throws SQLException {
		ps.setString(i, JsonUtils.serialize(parameter));
	}

	@Override
	public T getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return this.toObject(rs.getString(columnName), clazz);
	}

	@Override
	public T getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return this.toObject(rs.getString(columnIndex), clazz);
	}

	@Override
	public T getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return this.toObject(cs.getString(columnIndex), clazz);
	}

	private T toObject(String content, Class<T> clazz) {
		if (content != null) {
			return JsonUtils.deserialize(content, clazz);
		} else {
			return null;
		}
	}
}