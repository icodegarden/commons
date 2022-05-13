package io.github.icodegarden.commons.springboot.web.util;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.github.icodegarden.commons.lang.util.SystemUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public abstract class MappingJackson2HttpMessageConverters {

	/**
	 * 通常你需要 @Bean
	 * 
	 * @return
	 */
	public static MappingJackson2HttpMessageConverter simple() {
		MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
		ObjectMapper om = new ObjectMapper();
		om.setSerializationInclusion(Include.NON_NULL);
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		om.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		SimpleModule simpleModule = new SimpleModule();
//		simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
//		simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);

		simpleModule.addSerializer(Long.class, new JsonSerializer<Long>() {
			@Override
			public void serialize(Long l, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
					throws IOException {
				if (WebUtils.isInternalRpc()) {
					jsonGenerator.writeNumber(l);
				} else {
					jsonGenerator.writeString(l.toString());
				}
			}
		});

		om.registerModule(simpleModule);

		JavaTimeModule timeModule = new JavaTimeModule();
		timeModule.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
			@Override
			public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator,
					SerializerProvider serializerProvider) throws IOException {
				jsonGenerator.writeString(SystemUtils.STANDARD_DATETIME_FORMATTER.format(localDateTime));
			}
		});
		timeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
			@Override
			public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
					throws IOException, JsonProcessingException {
				String valueAsString = jsonParser.getValueAsString();
				return LocalDateTime.parse(valueAsString, SystemUtils.STANDARD_DATETIME_FORMATTER);
			}
		});
		om.registerModule(timeModule);

		jackson2HttpMessageConverter.setObjectMapper(om);
		return jackson2HttpMessageConverter;
	}
}