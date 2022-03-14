package io.github.icodegarden.commons.lang.serialization;

import io.github.icodegarden.commons.lang.serialization.StringDeserializer;
import io.github.icodegarden.commons.lang.serialization.StringSerializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class StringSerializerTests extends SerializerTests{
	
	String s = "test string Serializer";
	
	@Override
	protected Object getData() {
		return s;
	}
	
	@Override
	protected StringSerializer getSerializer() {
		return new StringSerializer();
	}
	
	@Override
	protected StringDeserializer getDeserializer() {
		return new StringDeserializer();
	}
	
}
