package io.github.icodegarden.commons.lang.serialization;
import org.junit.jupiter.api.Test;

import io.github.icodegarden.commons.lang.serialization.KryoDeserializer;
import io.github.icodegarden.commons.lang.serialization.KryoSerializer;
import io.github.icodegarden.commons.lang.serialization.SerializationException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class KryoSerializerTests extends SerializerTests {

	UserForTests d = new UserForTests("name", 18);

	@Override
	protected Object getData() {
		return d;
	}

	@Override
	protected KryoSerializer getSerializer() {
		return new KryoSerializer();//.registerClass(UserForTests.class);
	}

	@Override
	protected KryoDeserializer getDeserializer() {
		return new KryoDeserializer();//.registerClass(UserForTests.class);
	}

	@Test
	public void testDeseriaError() throws Exception {
		JavaSerializerTests javaObjectSerializerTests = new JavaSerializerTests();
		
		byte[] bytes = javaObjectSerializerTests.getSerializer().serialize(javaObjectSerializerTests.getData());
		
		try{
			Object deserialize = getDeserializer().deserialize(bytes);
			throw new RuntimeException("到这里失败");
		}catch (SerializationException e) {
		}
	}
}
