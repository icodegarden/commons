package io.github.icodegarden.commons.lang.serialization;

import io.github.icodegarden.commons.lang.serialization.ByteArrayDeserializer;
import io.github.icodegarden.commons.lang.serialization.ByteArraySerializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public class ByteArraySerializerTests extends SerializerTests{
	
	byte[] bs = {1,9,2,8,3,7,0,4,8,5};
	
	@Override
	protected Object getData() {
		return bs;
	}
	
	@Override
	protected ByteArraySerializer getSerializer() {
		return new ByteArraySerializer();
	}
	
	@Override
	protected ByteArrayDeserializer getDeserializer() {
		return new ByteArrayDeserializer();
	}
	
}
