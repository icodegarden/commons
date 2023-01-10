package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.caucho.hessian.io.Hessian2Input;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class Hessian2Deserializer implements Deserializer<Object> {

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		Hessian2Input input = new Hessian2Input(is);
		try {
			return input.readObject();
		} catch (IOException e) {
			throw new SerializationException("Error when deserializing byte[] to object", e);
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				log.error("ex on close hessian2 input", e);
			}
		}
	}
}