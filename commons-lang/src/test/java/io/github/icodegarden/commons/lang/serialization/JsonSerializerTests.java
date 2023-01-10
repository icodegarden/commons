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
public class JsonSerializerTests extends SerializerTests {

	UserForTests d = new UserForTests("name", 18);

	@Override
	protected Object getData() {
		return d;
	}

	@Override
	protected JsonSerializer getSerializer() {
		return new JsonSerializer();
	}

	@Override
	protected JsonDeserializer getDeserializer() {
		return new JsonDeserializer().setType(UserForTests.class);
	}
	
}
