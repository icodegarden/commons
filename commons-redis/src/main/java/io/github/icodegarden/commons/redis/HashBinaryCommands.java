package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.icodegarden.commons.redis.args.MapScanCursor;
import io.github.icodegarden.commons.redis.args.ScanArgs;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface HashBinaryCommands {

	/**
	 * <h1>删除1个或多个field，当没有field时删除hash</h1><br>
	 * 
	 * Removes the specified fields from the hash stored at key. Specified fields
	 * that do not exist within this hash are ignored. If key does not exist, it is
	 * treated as an empty hash and this command returns 0.
	 * 
	 * redis> HSET myhash field1 "foo"<br>
	 * (integer) 1<br>
	 * redis> HDEL myhash field1<br>
	 * (integer) 1<br>
	 * redis> HDEL myhash field2<br>
	 * (integer) 0<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	Long hdel(final byte[] key, final byte[]... fields);

	/**
	 * <h1>field是否存在</h1><br>
	 * 
	 * Returns if field is an existing field in the hash stored at key.
	 * 
	 * redis> HSET myhash field1 "foo" (integer) 1 redis> HEXISTS myhash field1
	 * (integer) 1 redis> HEXISTS myhash field2 (integer) 0 redis>
	 * 
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	Boolean hexists(final byte[] key, final byte[] field);

	/**
	 * <h1>获取field的值</h1><br>
	 * 
	 * Returns the value associated with field in the hash stored at key.
	 * 
	 * redis> HSET myhash field1 "foo"<br>
	 * (integer) 1<br>
	 * redis> HGET myhash field1<br>
	 * "foo"<br>
	 * redis> HGET myhash field2<br>
	 * (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	byte[] hget(final byte[] key, final byte[] field);

	/**
	 * <h1>获取所有field的值</h1><br>
	 * 
	 * Returns all fields and values of the hash stored at key. In the returned
	 * value, every field name is followed by its value, so the length of the reply
	 * is twice the size of the hash.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSET myhash field2 "World"<br>
	 * (integer) 1<br>
	 * redis> HGETALL myhash<br>
	 * 1) "field1"<br>
	 * 2) "Hello"<br>
	 * 3) "field2"<br>
	 * 4) "World"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	Map<byte[], byte[]> hgetAll(final byte[] key);

	/**
	 * <h1>对field进行指定的原子值操作，field不存在则初始设置0</h1><br>
	 * 
	 * Increments the number stored at field in the hash stored at key by increment.
	 * If key does not exist, a new key holding a hash is created. If field does not
	 * exist the value is set to 0 before the operation is performed.
	 * 
	 * The range of values supported by HINCRBY is limited to 64 bit signed
	 * integers.
	 * 
	 * redis> HSET myhash field 5<br>
	 * (integer) 1<br>
	 * redis> HINCRBY myhash field 1<br>
	 * (integer) 6<br>
	 * redis> HINCRBY myhash field -1<br>
	 * (integer) 5<br>
	 * redis> HINCRBY myhash field -10<br>
	 * (integer) -5<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	Long hincrBy(byte[] key, byte[] field, long value);

	/**
	 * <h1>对field进行指定的原子值操作，field不存在则初始设置0</h1><br>
	 * 
	 * Increment the specified field of a hash stored at key, and representing a
	 * floating point number, by the specified increment. If the increment value is
	 * negative, the result is to have the hash field value decremented instead of
	 * incremented. If the field does not exist, it is set to 0 before performing
	 * the operation. An error is returned if one of the following conditions occur:
	 * 
	 * The field contains a value of the wrong type (not a string). The current
	 * field content or the specified increment are not parsable as a double
	 * precision floating point number. The exact behavior of this command is
	 * identical to the one of the INCRBYFLOAT command, please refer to the
	 * documentation of INCRBYFLOAT for further information.
	 * 
	 * redis> HSET mykey field 10.50<br>
	 * (integer) 1<br>
	 * redis> HINCRBYFLOAT mykey field 0.1<br>
	 * "10.6"<br>
	 * redis> HINCRBYFLOAT mykey field -5<br>
	 * "5.6"<br>
	 * redis> HSET mykey field 5.0e3<br>
	 * (integer) 0<br>
	 * redis> HINCRBYFLOAT mykey field 2.0e2<br>
	 * "5200"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	Double hincrByFloat(byte[] key, byte[] field, double value);

	/**
	 * <h1>获取所有field的名称</h1><br>
	 * 
	 * Returns all field names in the hash stored at key.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSET myhash field2 "World"<br>
	 * (integer) 1<br>
	 * redis> HKEYS myhash<br>
	 * 1) "field1"<br>
	 * 2) "field2"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	Set<byte[]> hkeys(final byte[] key);

	/**
	 * <h1>获取field的总数</h1><br>
	 * 
	 * Returns the number of fields contained in the hash stored at key.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSET myhash field2 "World"<br>
	 * (integer) 1<br>
	 * redis> HLEN myhash<br>
	 * (integer) 2<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	Long hlen(final byte[] key);

	/**
	 * <h1>批量获取field的值</h1><br>
	 * 
	 * Returns the values associated with the specified fields in the hash stored at
	 * key.
	 * 
	 * For every field that does not exist in the hash, a nil value is returned.
	 * Because non-existing keys are treated as empty hashes, running HMGET against
	 * a non-existing key will return a list of nil values.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSET myhash field2 "World"<br>
	 * (integer) 1<br>
	 * redis> HMGET myhash field1 field2 nofield<br>
	 * 1) "Hello"<br>
	 * 2) "World"<br>
	 * 3) (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param fields
	 * @return
	 */
	List<byte[]> hmget(final byte[] key, final byte[]... fields);

	/**
	 * <h1>批量设置field的值</h1><br>
	 * 
	 * Sets the specified fields to their respective values in the hash stored at
	 * key. This command overwrites any specified fields already existing in the
	 * hash. If key does not exist, a new key holding a hash is created. <br>
	 * 
	 * redis> HMSET myhash field1 "Hello" field2 "World"<br>
	 * "OK"<br>
	 * redis> HGET myhash field1<br>
	 * "Hello"<br>
	 * redis> HGET myhash field2<br>
	 * "World"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param hash
	 * @return
	 */
	@Deprecated
	String hmset(final byte[] key, final Map<byte[], byte[]> hash);

	byte[] hrandfield(final byte[] key);

	List<byte[]> hrandfield(final byte[] key, final long count);

	/**
	 * <h1>随机获取1个或多个field的值</h1><br>
	 * 
	 * When called with just the key argument, return a random field from the hash
	 * value stored at key.
	 * 
	 * If the provided count argument is positive, return an array of distinct
	 * fields. The array's length is either count or the hash's number of fields
	 * (HLEN), whichever is lower.
	 * 
	 * If called with a negative count, the behavior changes and the command is
	 * allowed to return the same field multiple times. In this case, the number of
	 * returned fields is the absolute value of the specified count.
	 * 
	 * The optional WITHVALUES modifier changes the reply so it includes the
	 * respective values of the randomly selected hash fields.
	 * 
	 * redis> HMSET coin heads obverse tails reverse edge null<br>
	 * "OK"<br>
	 * redis> HRANDFIELD coin<br>
	 * "edge"<br>
	 * redis> HRANDFIELD coin<br>
	 * "heads"<br>
	 * redis> HRANDFIELD coin -5 WITHVALUES<br>
	 * 1) "tails"<br>
	 * 2) "reverse"<br>
	 * 3) "heads"<br>
	 * 4) "obverse"<br>
	 * 5) "heads"<br>
	 * 6) "obverse"<br>
	 * 7) "tails"<br>
	 * 8) "reverse"<br>
	 * 9) "edge"<br>
	 * 10) "null"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param count
	 * @return
	 */
	Map<byte[], byte[]> hrandfieldWithValues(final byte[] key, final long count);

	MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor);

	/**
	 * <h1>迭代所有field的值</h1><br>
	 * 
	 * See SCAN for HSCAN documentation.
	 * 
	 * 
	 * @param key
	 * @param cursor
	 * @return
	 */
	MapScanCursor<byte[], byte[]> hscan(byte[] key, byte[] cursor, ScanArgs params);

	Long hset(final byte[] key, final byte[] field, final byte[] value);
	/**
	 * <h1>创建或修改field的值</h1><br>
	 * 
	 * Sets the specified fields to their respective values in the hash stored at
	 * key.
	 * 
	 * This command overwrites the values of specified fields that exist in the
	 * hash. If key doesn't exist, a new key holding a hash is created.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HGET myhash field1<br>
	 * "Hello"<br>
	 * redis> HSET myhash field2 "Hi" field3 "World"<br>
	 * (integer) 2<br>
	 * redis> HGET myhash field2<br>
	 * "Hi"<br>
	 * redis> HGET myhash field3<br>
	 * "World"<br>
	 * redis> HGETALL myhash<br>
	 * 1) "field1"<br>
	 * 2) "Hello"<br>
	 * 3) "field2"<br>
	 * 4) "Hi"<br>
	 * 5) "field3"<br>
	 * 6) "World"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param hash
	 * @return
	 */
	Long hset(final byte[] key, final Map<byte[], byte[]> hash);

	/**
	 * <h1>只有当field不存在时设置值</h1><br>
	 * 
	 * Sets field in the hash stored at key to value, only if field does not yet
	 * exist. If key does not exist, a new key holding a hash is created. If field
	 * already exists, this operation has no effect.
	 * 
	 * redis> HSETNX myhash field "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSETNX myhash field "World"<br>
	 * (integer) 0<br>
	 * redis> HGET myhash field<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 */
	Long hsetnx(final byte[] key, final byte[] field, final byte[] value);

	/**
	 * <h1>获取field的值的长度</h1><br>
	 * 
	 * Returns the string length of the value associated with field in the hash
	 * stored at key. If the key or the field do not exist, 0 is returned.
	 * 
	 * redis> HMSET myhash f1 HelloWorld f2 99 f3 -256<br>
	 * "OK"<br>
	 * redis> HSTRLEN myhash f1<br>
	 * (integer) 10<br>
	 * redis> HSTRLEN myhash f2<br>
	 * (integer) 2<br>
	 * redis> HSTRLEN myhash f3<br>
	 * (integer) 4<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	Long hstrlen(final byte[] key, final byte[] field);

	/**
	 * <h1>获取所有field的值</h1><br>
	 * 
	 * Returns all values in the hash stored at key.
	 * 
	 * redis> HSET myhash field1 "Hello"<br>
	 * (integer) 1<br>
	 * redis> HSET myhash field2 "World"<br>
	 * (integer) 1<br>
	 * redis> HVALS myhash<br>
	 * 1) "Hello"<br>
	 * 2) "World"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	List<byte[]> hvals(final byte[] key);
}
