package io.github.icodegarden.commons.nio;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Deserializer;
import io.github.icodegarden.commons.lang.serialization.Hessian2Serializer;
import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;
import lombok.Getter;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Getter
public enum SerializerType {

	Kryo(0, new KryoSerializer(), new KryoDeserializer()), //
	Jdk(1, new JavaSerializer(), new JavaDeserializer()),//
	Hessian2(2, new Hessian2Serializer(), new Hessian2Deserializer()),//
//	Json(3, new JsonSerializer(), new JsonDeserializer()), //
	;

	private final int value;
	private final Serializer<Object> serializer;
	private final Deserializer<Object> deserializer;

	private SerializerType(int value, Serializer<Object> serializer, Deserializer<Object> deserializer) {
		this.value = value;
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	public static SerializerType get(int value) {
		SerializerType[] serializerTypes = SerializerType.values();
		for (SerializerType serializerType : serializerTypes) {
			if (serializerType.getValue() == value) {
				return serializerType;
			}
		}
		throw new IllegalArgumentException("SerializerType of value:" + value + " Not Support");
	}
}
