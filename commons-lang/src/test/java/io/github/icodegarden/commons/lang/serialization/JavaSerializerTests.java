package io.github.icodegarden.commons.lang.serialization;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.serialization.JavaDeserializer;
import io.github.icodegarden.commons.lang.serialization.JavaSerializer;
import io.github.icodegarden.commons.lang.serialization.SerializationException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class JavaSerializerTests extends SerializerTests {

	UserForTests d = new UserForTests("name", 18);

	@Override
	protected Object getData() {
		return d;
	}

	@Override
	protected JavaSerializer getSerializer() {
		return new JavaSerializer();
	}

	@Override
	protected JavaDeserializer getDeserializer() {
		return new JavaDeserializer();
	}

	@Test
	public void testDeseria() throws Exception {
		JavaDeserializer deserializer = getDeserializer();
		//该bytes没有active字段，测试兼容性
		Object object = deserializer.deserialize(new byte[] {-84, -19, 0, 5, 115, 114, 0, 61, 105, 111, 46, 103, 105, 116, 104, 117, 98, 46, 105, 99, 111, 100, 101, 103, 97, 114, 100, 101, 110, 46, 99, 111, 109, 109, 111, 110, 115, 46, 108, 97, 110, 103, 46, 115, 101, 114, 105, 97, 108, 105, 122, 97, 116, 105, 111, 110, 46, 85, 115, 101, 114, 70, 111, 114, 84, 101, 115, 116, 115, 0, 0, 0, 0, 0, 0, 0, 1, 2, 0, 2, 73, 0, 3, 97, 103, 101, 76, 0, 4, 110, 97, 109, 101, 116, 0, 18, 76, 106, 97, 118, 97, 47, 108, 97, 110, 103, 47, 83, 116, 114, 105, 110, 103, 59, 120, 112, 0, 0, 0, 18, 116, 0, 4, 110, 97, 109, 101});
		System.out.println(object);
	}
	
	@Test
	public void testDeseriaError() throws Exception {
		KryoSerializerTests kryoObjectSerializerTests = new KryoSerializerTests();
		
		byte[] bytes = kryoObjectSerializerTests.getSerializer().serialize(kryoObjectSerializerTests.getData());
		
		try{
			Object deserialize = getDeserializer().deserialize(bytes);
			throw new RuntimeException("到这里失败");
		}catch (SerializationException e) {
		}
	}

}
