package io.github.icodegarden.commons.lang.serialization;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
public interface Deserializer<T> {

	/**
	 * Deserialize a record value from a byte array into a value or object.
	 * 
	 * @param notnull
	 * @return deserialized typed data; may be null
	 * @throws SerializationException throw on serialization error
	 */
	T deserialize(byte[] data) throws SerializationException;
}