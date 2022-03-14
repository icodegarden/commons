package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class JavaDeserializer implements Deserializer<Object> {

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException{
		try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));) {
			return objectInputStream.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new SerializationException("Error when deserializing byte[] to object", e);
		}
	}
}