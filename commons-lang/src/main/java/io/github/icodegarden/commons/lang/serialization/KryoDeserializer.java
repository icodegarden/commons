package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayInputStream;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;

import io.github.icodegarden.commons.lang.serialization.KryoSerializer.AbstractKryoFactory;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer.ThreadLocalKryoFactory;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class KryoDeserializer implements Deserializer<Object> {

	private static AbstractKryoFactory kryoFactory = new ThreadLocalKryoFactory();

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		Kryo kryo = kryoFactory.getKryo();

		ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
		try (Input input = new Input(byteArrayInputStream);) {
			return kryo.readClassAndObject(input);
//			return kryo.readObject(input, Object.class);
		} catch (KryoException e) {
			throw new SerializationException("Error when deserializing byte[] to object", e);
		}
	}

}