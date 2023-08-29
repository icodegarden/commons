package io.github.icodegarden.commons.lang.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * 
 * @author Fangfang.Xu
 */
public abstract class JsonUtils {
	private JsonUtils() {
	}

	public static final DateTimeFormatter STANDARD_DATETIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd HH:mm:ss");

	private static ObjectMapper om = new ObjectMapper();
	static {
		om = new ObjectMapper();
		om.setSerializationInclusion(Include.NON_NULL);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.configure(SerializationFeature.FLUSH_AFTER_WRITE_VALUE, true);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
					SerializerProvider serializerProvider) throws IOException {
				jsonGenerator.writeString(STANDARD_DATETIME_FORMATTER.format(localDateTime));
			}
		});
		timeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException, JsonProcessingException {
				String valueAsString = jsonParser.getValueAsString();
				return LocalDateTime.parse(valueAsString, STANDARD_DATETIME_FORMATTER);
			}
		});
		om.registerModule(timeModule);
	}

	public static String serialize(Object obj) throws IllegalArgumentException {
		try {
			return om.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("serialize json error", e);
		}
	}

	public static <T> T deserialize(String jsonObject, Class<T> cla) throws IllegalArgumentException {
		try {
			return om.readValue(jsonObject, cla);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	/**
	 * 可以反序列化为jsonObject或jsonArray，取决于{@link ParameterizedTypeReference<T>}的泛型类型
	 * 
	 * @param <T>
	 * @param json
	 * @param typeReference
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static <T> T deserialize(String json, ParameterizedTypeReference<T> typeReference)
			throws IllegalArgumentException {
		Type responseType = typeReference.getType();
		return deserialize(json, responseType);
	}

	/**
	 * 可以反序列化为jsonObject或jsonArray，取决于responseType的真实类型
	 * 
	 * @param <T>
	 * @param json
	 * @param responseType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static <T> T deserialize(String json, Type responseType) throws IllegalArgumentException {
		try {
			JavaType javaType = om.constructType(responseType);
			return om.readValue(json, javaType);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	/**
	 * 可以反序列化为jsonObject或jsonArray，取决于responseType的真实类型
	 * 
	 * @param <T>
	 * @param json
	 * @param responseType
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static <T> T deserialize(InputStream in, Type responseType) throws IllegalArgumentException {
		try {
			JavaType javaType = om.constructType(responseType);
			return om.readValue(in, javaType);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	public static <T> List<T> deserializeArray(String jsonArray, Class<T> cla) throws IllegalArgumentException {
		try {
			JavaType javaType = om.getTypeFactory().constructParametricType(ArrayList.class, cla);
			return om.readValue(jsonArray, javaType);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	/**
	 * 
	 * @param <P>
	 * @param jsonArray
	 * @param cla
	 * @param parametrized 某集合的类型
	 * @return
	 * @throws IllegalArgumentException
	 */
	public static <P> P deserializeArray(String jsonArray, Class<?> cla, Class<P> parametrized)
			throws IllegalArgumentException {
		try {
			JavaType javaType = om.getTypeFactory().constructParametricType(parametrized, cla);
			return om.readValue(jsonArray, javaType);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	public static <K, V> Map<K, V> deserializeMap(String jsonObject, Class<K> k, Class<V> v)
			throws IllegalArgumentException {
		try {
			JavaType javaType = om.getTypeFactory().constructParametricType(Map.class, k, v);
			return om.readValue(jsonObject, javaType);
		} catch (Exception e) {
			throw new IllegalArgumentException("deserialize json error", e);
		}
	}

	public static ObjectMapper getObjectMapper() {
		return om;
	}

}