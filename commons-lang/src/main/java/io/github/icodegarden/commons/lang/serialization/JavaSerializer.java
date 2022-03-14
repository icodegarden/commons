package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaSerializer implements Serializer<Object> {

	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);) {
			objectOutputStream.writeObject(obj);
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new SerializationException("Error when serializing object to byte[]", e);
		}
	}
}