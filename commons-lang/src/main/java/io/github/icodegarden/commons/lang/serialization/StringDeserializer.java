package io.github.icodegarden.commons.lang.serialization;

import java.io.UnsupportedEncodingException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class StringDeserializer implements Deserializer<String> {
	private String encoding = "UTF8";

	@Override
	public String deserialize(byte[] data) throws SerializationException {
		try {
			if (data == null)
				return null;
			else
				return new String(data, encoding);
		} catch (UnsupportedEncodingException e) {
			throw new SerializationException(
					"Error when deserializing byte[] to string due to unsupported encoding " + encoding);
		}
	}
}