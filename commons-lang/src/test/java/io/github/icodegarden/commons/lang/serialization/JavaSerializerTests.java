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
