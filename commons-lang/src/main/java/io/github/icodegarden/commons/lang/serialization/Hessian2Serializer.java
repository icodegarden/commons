package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Output;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class Hessian2Serializer implements Serializer<Object> {

	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		Hessian2Output output = new Hessian2Output(os);
		try {
			output.writeObject(obj);
			output.flushBuffer();

			return os.toByteArray();
		} catch (IOException e) {
			throw new SerializationException("Error when serializing object to byte[]", e);
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				log.error("ex on close hessian2 output", e);
			}
		}
	}
}