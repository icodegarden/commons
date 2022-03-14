package io.github.icodegarden.commons.lang.serialization;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class StringSerializer implements Serializer<String> {
	private String encoding = "UTF8";

	@Override
	public byte[] serialize(String data) throws SerializationException {
		try {
			if (data == null)
				return null;
			else
				return data.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(
					"Error when serializing string to byte[] due to unsupported encoding " + encoding);
		}
	}

}