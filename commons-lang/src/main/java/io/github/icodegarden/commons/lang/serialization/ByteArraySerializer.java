package io.github.icodegarden.commons.lang.serialization;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ByteArraySerializer implements Serializer<byte[]> {

	@Override
	public byte[] serialize(byte[] data) throws SerializationException {
		return data;
	}
}