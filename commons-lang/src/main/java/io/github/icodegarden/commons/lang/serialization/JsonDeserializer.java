package io.github.icodegarden.commons.lang.serialization;

import org.springframework.util.Assert;

import io.github.icodegarden.commons.lang.util.JsonUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JsonDeserializer implements Deserializer<Object> {

	private final StringDeserializer deserializer = new StringDeserializer();
	private Class<?> type;

	public JsonDeserializer setType(Class<?> type) {
		this.type = type;
		return this;
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		Assert.notNull(type, "type must not null");
		String json = deserializer.deserialize(bytes);
		return JsonUtils.deserialize(json, type);
	}

	public Object deserialize(byte[] bytes, Class<?> type) throws SerializationException {
		String json = deserializer.deserialize(bytes);
		return JsonUtils.deserialize(json, type);
	}
}