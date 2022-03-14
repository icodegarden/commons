package io.github.icodegarden.commons.mybatis.handler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
public class JsonArrayHandler<T extends Object> extends BaseTypeHandler<List<T>> {
	private Class<T> clazz;

	public JsonArrayHandler(Class<T> clazz) {
		Objects.requireNonNull(clazz, "Type argument cannot be null");
		this.clazz = clazz;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, List<T> parameter, JdbcType jdbcType)
			throws SQLException {
		ps.setString(i, JsonUtils.serialize(parameter));
	}

	@Override
	public List<T> getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return this.toObject(rs.getString(columnName), clazz);
	}

	@Override
	public List<T> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return this.toObject(rs.getString(columnIndex), clazz);
	}

	@Override
	public List<T> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return this.toObject(cs.getString(columnIndex), clazz);
	}

	private List<T> toObject(String content, Class<T> clazz) {
		if (content != null) {
			return JsonUtils.deserializeArray(content, clazz);
		} else {
			return null;
		}
	}
}