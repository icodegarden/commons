package io.github.icodegarden.commons.lang.serialization;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class ByteArrayDeserializer implements Deserializer<byte[]> {

	@Override
	public byte[] deserialize(byte[] data) throws SerializationException {
		return data;
	}
}