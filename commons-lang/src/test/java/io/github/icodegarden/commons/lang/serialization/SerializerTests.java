package io.github.icodegarden.commons.lang.serialization;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.icodegarden.commons.lang.serialization.Deserializer;
import io.github.icodegarden.commons.lang.serialization.Serializer;

/**
 * 
 * @author Fangfang.Xu
 *
 */
//@RunWith(MockitoJUnitRunner.class)
public abstract class SerializerTests {
	
	private static Logger log = LoggerFactory.getLogger(SerializerTests.class);
	
	protected abstract Object getData();
	protected abstract Serializer getSerializer();
	protected abstract Deserializer getDeserializer();
	
	@Test
	public void testSeriaDeseria() throws Exception {
		Serializer serializer = getSerializer();
		Deserializer deserializer = getDeserializer();
		Object data = getData();
		
		byte[] bytes = serializer.serialize(data);
		Object deserialize = deserializer.deserialize(bytes);
		assertEquals(data, deserialize);
	}
	
	@Test
	public void testPerformance() throws Exception {
		int loop = 100000;
		long start = System.currentTimeMillis();
		for(int i=0;i<loop;i++) {
			Serializer serializer = getSerializer();
			Deserializer deserializer = getDeserializer();
			Object data = getData();
			
			byte[] bytes = serializer.serialize(data);
			Object deserialize = deserializer.deserialize(bytes);
		}
		long end = System.currentTimeMillis();
		log.info("performance "+this.getClass().getName()+" loop for "+loop+" used millis:"+(end-start));
	}

}
