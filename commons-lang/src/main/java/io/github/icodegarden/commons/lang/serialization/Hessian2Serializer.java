package io.github.icodegarden.commons.lang.serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;

import com.caucho.hessian.io.AbstractDeserializer;
import com.caucho.hessian.io.AbstractHessianInput;
import com.caucho.hessian.io.AbstractHessianOutput;
import com.caucho.hessian.io.AbstractSerializer;
import com.caucho.hessian.io.ExtSerializerFactory;
import com.caucho.hessian.io.Hessian2Output;
import com.caucho.hessian.io.SerializerFactory;

import io.github.icodegarden.commons.lang.util.SystemUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * @author Fangfang.Xu
 *
 */
@Slf4j
public class Hessian2Serializer implements Serializer<Object> {

	static SerializerFactory serializerFactory = new SerializerFactory();

	static {
		ExtSerializerFactory extSerializerFactory = new ExtSerializerFactory();
		/*
		 * 对LocalDateTime进行序列化
		 */
		extSerializerFactory.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
		/*
		 * 对LocalDateTime进行反序列化
		 */
		extSerializerFactory.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

		serializerFactory.addFactory(extSerializerFactory);
	}
	
	/**
	 * 自定义备用
	 * @param serializerFactory
	 */
	public static void configSerializerFactory(SerializerFactory serializerFactory) {
		Hessian2Serializer.serializerFactory = serializerFactory;
	}

	@Override
	public byte[] serialize(Object obj) throws SerializationException {
		ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
		Hessian2Output output = new Hessian2Output(os);
		output.setSerializerFactory(serializerFactory);
		try {
			output.writeObject(obj);
			output.flushBuffer();

			return os.toByteArray();
		} catch (Throwable e) {
			throw new SerializationException("Error when serializing object to byte[]", e);
		} finally {
			try {
				output.close();
			} catch (IOException e) {
				log.error("ex on close hessian2 output", e);
			}
		}
	}

	static class LocalDateTimeSerializer extends AbstractSerializer {

		@Override
		public void writeObject(Object obj, AbstractHessianOutput out) throws IOException {
			if (obj == null) {
				out.writeNull();
			} else {
				Class<?> cl = obj.getClass();

				if (out.addRef(obj)) {
					return;
				}
				// ref 返回-2 便是开始写Map
				int ref = out.writeObjectBegin(cl.getName());

				if (ref < -1) {
					out.writeString("value");

					String time = SystemUtils.STANDARD_DATETIMEMS_FORMATTER.format((LocalDateTime) obj);
					out.writeString(time);

					out.writeMapEnd();
				} else {
					if (ref == -1) {
						out.writeInt(1);
						out.writeString("value");
						out.writeObjectBegin(cl.getName());
					}

					String time = SystemUtils.STANDARD_DATETIMEMS_FORMATTER.format((LocalDateTime) obj);
					out.writeString(time);
				}
			}
		}
	}

	static class LocalDateTimeDeserializer extends AbstractDeserializer {

		@Override
		public Class<?> getType() {
			return LocalDateTime.class;
		}

		@Override
		public Object readObject(AbstractHessianInput in, Object[] fields) throws IOException {
			String[] fieldNames = (String[]) fields;
			int ref = in.addRef(null);
			String time = null;
			for (String key : fieldNames) {
				if ("value".equals(key)) {
					time = in.readString();
				} else {
					in.readObject();
				}
			}
			Object value = create(time);
			in.setRef(ref, value);
			return value;
		}

		private Object create(String time) throws IOException {
			return LocalDateTime.parse(time, SystemUtils.STANDARD_DATETIMEMS_FORMATTER);
		}

	}
}