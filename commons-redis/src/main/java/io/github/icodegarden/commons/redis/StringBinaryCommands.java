package io.github.icodegarden.commons.redis;

import java.util.List;

import io.github.icodegarden.commons.redis.args.GetExArgs;
import io.github.icodegarden.commons.redis.args.LCSMatchResult;
import io.github.icodegarden.commons.redis.args.LCSParams;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface StringBinaryCommands {

	/**
	 * <h1>追加字符串，如果key不存在则创建</h1><br>
	 * 
	 * If key already exists and is a string, this command appends the value at the
	 * end of the string. If key does not exist it is created and set as an empty
	 * string, so APPEND will be similar to SET in this special case.
	 * 
	 * redis> EXISTS mykey<br>
	 * (integer) 0<br>
	 * redis> APPEND mykey "Hello"<br>
	 * (integer) 5<br>
	 * redis> APPEND mykey " World"<br>
	 * (integer) 11<br>
	 * redis> GET mykey<br>
	 * "Hello World"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	Long append(byte[] key, byte[] value);

	/**
	 * <h1>原子减少1，如果key不存在则初始为0值</h1><br>
	 * Decrements the number stored at key by one. If the key does not exist, it is
	 * set to 0 before performing the operation. An error is returned if the key
	 * contains a value of the wrong type or contains a string that can not be
	 * represented as integer. This operation is limited to 64 bit signed integers.
	 * 
	 * See INCR for extra information on increment/decrement operations.
	 * 
	 * redis> SET mykey "10"<br>
	 * "OK"<br>
	 * redis> DECR mykey<br>
	 * (integer) 9<br>
	 * redis> SET mykey "234293482390480948029348230948"<br>
	 * "OK"<br>
	 * redis> DECR mykey<br>
	 * (error) value is not an integer or out of range<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	Long decr(byte[] key);

	/**
	 * <h1>原子减少指定值，如果key不存在则初始为0值</h1><br>
	 * 
	 * Decrements the number stored at key by decrement. If the key does not exist,
	 * it is set to 0 before performing the operation. An error is returned if the
	 * key contains a value of the wrong type or contains a string that can not be
	 * represented as integer. This operation is limited to 64 bit signed integers.
	 * 
	 * See INCR for extra information on increment/decrement operations.
	 * 
	 * redis> SET mykey "10"<br>
	 * "OK"<br>
	 * redis> DECRBY mykey 3<br>
	 * (integer) 7<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	Long decrBy(byte[] key, long value);

	/**
	 * <h1>获取值</h1><br>
	 * 
	 * Get the value of key. If the key does not exist the special value nil is
	 * returned. An error is returned if the value stored at key is not a string,
	 * because GET only handles string values.
	 * 
	 * redis> GET nonexisting<br>
	 * (nil)<br>
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> GET mykey<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	byte[] get(final byte[] key);

	/**
	 * <h1>获取值并删除key</h1><br>
	 * 
	 * Get the value of key and delete the key. This command is similar to GET,
	 * except for the fact that it also deletes the key on success (if and only if
	 * the key's value type is a string).
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> GETDEL mykey<br>
	 * "Hello"<br>
	 * redis> GET mykey<br>
	 * (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	byte[] getDel(byte[] key);

	/**
	 * <h1>设置过期时间并返回值</h1><br>
	 * 
	 * Get the value of key and optionally set its expiration. GETEX is similar to
	 * GET, but is a write command with additional options.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> GETEX mykey<br>
	 * "Hello"<br>
	 * redis> TTL mykey<br>
	 * (integer) -1<br>
	 * redis> GETEX mykey EX 60<br>
	 * "Hello"<br>
	 * redis> TTL mykey<br>
	 * (integer) 60<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param params
	 * @return
	 */
	byte[] getEx(byte[] key, GetExArgs params);

	/**
	 * <h1>获取字符串的subString值</h1><br>
	 * 
	 * Returns the substring of the string value stored at key, determined by the
	 * offsets start and end (both are inclusive). Negative offsets can be used in
	 * order to provide an offset starting from the end of the string. So -1 means
	 * the last character, -2 the penultimate and so forth.
	 * 
	 * The function handles out of range requests by limiting the resulting range to
	 * the actual length of the string.
	 * 
	 * redis> SET mykey "This is a string"<br>
	 * "OK"<br>
	 * redis> GETRANGE mykey 0 3<br>
	 * "This"<br>
	 * redis> GETRANGE mykey -3 -1<br>
	 * "ing"<br>
	 * redis> GETRANGE mykey 0 -1<br>
	 * "This is a string"<br>
	 * redis> GETRANGE mykey 10 100<br>
	 * "string"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param startOffset
	 * @param endOffset
	 * @return
	 */
	byte[] getrange(byte[] key, long startOffset, long endOffset);

	/**
	 * <h1>获取旧值，设置新值</h1><br>
	 * 
	 * Atomically sets key to value and returns the old value stored at key. Returns
	 * an error when key exists but does not hold a string value. Any previous time
	 * to live associated with the key is discarded on successful SET operation.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> GETSET mykey "World"<br>
	 * "Hello"<br>
	 * redis> GET mykey<br>
	 * "World"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	byte[] getSet(byte[] key, byte[] value);

	/**
	 * <h1>原子增加1，如果key不存在则初始为0值</h1><br>
	 * Increments the number stored at key by one. If the key does not exist, it is
	 * set to 0 before performing the operation. An error is returned if the key
	 * contains a value of the wrong type or contains a string that can not be
	 * represented as integer. This operation is limited to 64 bit signed integers.
	 * 
	 * Note: this is a string operation because Redis does not have a dedicated
	 * integer type. The string stored at the key is interpreted as a base-10 64 bit
	 * signed integer to execute the operation.
	 * 
	 * Redis stores integers in their integer representation, so for string values
	 * that actually hold an integer, there is no overhead for storing the string
	 * representation of the integer.
	 * 
	 * redis> SET mykey "10"<br>
	 * "OK"<br>
	 * redis> INCR mykey<br>
	 * (integer) 11<br>
	 * redis> GET mykey<br>
	 * "11"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	Long incr(byte[] key);

	/**
	 * <h1>原子增加指定值，如果key不存在则初始为0值</h1><br>
	 * 
	 * Increments the number stored at key by increment. If the key does not exist,
	 * it is set to 0 before performing the operation. An error is returned if the
	 * key contains a value of the wrong type or contains a string that can not be
	 * represented as integer. This operation is limited to 64 bit signed integers.
	 * 
	 * See INCR for extra information on increment/decrement operations.
	 * 
	 * redis> SET mykey "10"<br>
	 * "OK"<br>
	 * redis> INCRBY mykey 5<br>
	 * (integer) 15<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param increment
	 * @return
	 */
	Long incrBy(byte[] key, long increment);

	/**
	 * <h1>原子增加指定值，如果key不存在则初始为0值</h1><br>
	 * 
	 * Increment the string representing a floating point number stored at key by
	 * the specified increment. By using a negative increment value, the result is
	 * that the value stored at the key is decremented (by the obvious properties of
	 * addition). If the key does not exist, it is set to 0 before performing the
	 * operation. An error is returned if one of the following conditions occur:
	 * 
	 * The key contains a value of the wrong type (not a string). The current key
	 * content or the specified increment are not parsable as a double precision
	 * floating point number. If the command is successful the new incremented value
	 * is stored as the new value of the key (replacing the old one), and returned
	 * to the caller as a string.
	 * 
	 * Both the value already contained in the string key and the increment argument
	 * can be optionally provided in exponential notation, however the value
	 * computed after the increment is stored consistently in the same format, that
	 * is, an integer number followed (if needed) by a dot, and a variable number of
	 * digits representing the decimal part of the number. Trailing zeroes are
	 * always removed.
	 * 
	 * The precision of the output is fixed at 17 digits after the decimal point
	 * regardless of the actual internal precision of the computation.
	 * 
	 * redis> SET mykey 10.50<br>
	 * "OK"<br>
	 * redis> INCRBYFLOAT mykey 0.1<br>
	 * "10.6"<br>
	 * redis> INCRBYFLOAT mykey -5<br>
	 * "5.6"<br>
	 * redis> SET mykey 5.0e3<br>
	 * "OK"<br>
	 * redis> INCRBYFLOAT mykey 2.0e2<br>
	 * "5200"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param increment
	 * @return
	 */
	Double incrByFloat(byte[] key, double increment);

	/**
	 * <h1>Finds the longest common substring.</h1><br>
	 * 
	 * The LCS command implements the longest common subsequence algorithm. Note
	 * that this is different than the longest common string algorithm, since
	 * matching characters in the string does not need to be contiguous.
	 * 
	 * For instance the LCS between "foo" and "fao" is "fo", since scanning the two
	 * strings from left to right, the longest common set of characters is composed
	 * of the first "f" and then the "o".
	 * 
	 * LCS is very useful in order to evaluate how similar two strings are. Strings
	 * can represent many things. For instance if two strings are DNA sequences, the
	 * LCS will provide a measure of similarity between the two DNA sequences. If
	 * the strings represent some text edited by some user, the LCS could represent
	 * how different the new text is compared to the old one, and so forth.
	 * 
	 * Note that this algorithm runs in O(N*M) time, where N is the length of the
	 * first string and M is the length of the second string. So either spin a
	 * different Redis instance in order to run this algorithm, or make sure to run
	 * it against very small strings.
	 * 
	 * > MSET key1 ohmytext key2 mynewtext<br>
	 * OK<br>
	 * > LCS key1 key2<br>
	 * "mytext"<br>
	 * Sometimes we need just the length of the match:<br>
	 * 
	 * > LCS key1 key2 LEN<br>
	 * (integer) 6<br>
	 * However what is often very useful, is to know the match position in each
	 * strings:<br>
	 * 
	 * > LCS key1 key2 IDX<br>
	 * 1) "matches"<br>
	 * 2) 1) 1) 1) (integer) 4<br>
	 * 2) (integer) 7<br>
	 * 2) 1) (integer) 5<br>
	 * 2) (integer) 8<br>
	 * 2) 1) 1) (integer) 2<br>
	 * 2) (integer) 3<br>
	 * 2) 1) (integer) 0<br>
	 * 2) (integer) 1<br>
	 * 3) "len"<br>
	 * 4) (integer) 6<br>
	 * Matches are produced from the last one to the first one, since this is how
	 * the algorithm works, and it more efficient to emit things in the same order.
	 * The above array means that the first match (second element of the array) is
	 * between positions 2-3 of the first string and 0-1 of the second. Then there
	 * is another match between 4-7 and 5-8.<br>
	 * 
	 * To restrict the list of matches to the ones of a given minimal length:<br>
	 * 
	 * > LCS key1 key2 IDX MINMATCHLEN 4<br>
	 * 1) "matches"<br>
	 * 2) 1) 1) 1) (integer) 4<br>
	 * 2) (integer) 7<br>
	 * 2) 1) (integer) 5<br>
	 * 2) (integer) 8<br>
	 * 3) "len"<br>
	 * 4) (integer) 6<br>
	 * Finally to also have the match len:<br>
	 * 
	 * > LCS key1 key2 IDX MINMATCHLEN 4 WITHMATCHLEN<br>
	 * 1) "matches"<br>
	 * 2) 1) 1) 1) (integer) 4<br>
	 * 2) (integer) 7<br>
	 * 2) 1) (integer) 5<br>
	 * 2) (integer) 8<br>
	 * 3) (integer) 4<br>
	 * 3) "len"<br>
	 * 4) (integer) 6<br>
	 * 
	 * @param keyA
	 * @param keyB
	 * @param params
	 * @return
	 */
	LCSMatchResult lcs(byte[] keyA, byte[] keyB, LCSParams params);

	/**
	 * <h1>获取1个或多个key的值</h1><br>
	 * 
	 * Returns the values of all specified keys. For every key that does not hold a
	 * string value or does not exist, the special value nil is returned. Because of
	 * this, the operation never fails.
	 * 
	 * redis> SET key1 "Hello"<br>
	 * "OK"<br>
	 * redis> SET key2 "World"<br>
	 * "OK"<br>
	 * redis> MGET key1 key2 nonexisting<br>
	 * 1) "Hello"<br>
	 * 2) "World"<br>
	 * 3) (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keys
	 * @return
	 */
	List<byte[]> mget(final byte[]... keys);

	/**
	 * <h1>修改各个key的值，不存在的key创建</h1><br>
	 * 
	 * Sets the given keys to their respective values. MSET replaces existing values
	 * with new values, just as regular SET. See MSETNX if you don't want to
	 * overwrite existing values.
	 * 
	 * MSET is atomic, so all given keys are set at once. It is not possible for
	 * clients to see that some of the keys were updated while others are unchanged.
	 * 
	 * redis> MSET key1 "Hello" key2 "World"<br>
	 * "OK"<br>
	 * redis> GET key1<br>
	 * "Hello"<br>
	 * redis> GET key2<br>
	 * "World"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keysvalues
	 * @return
	 */
	String mset(final byte[]... keysvalues);

	/**
	 * <h1>只有当所有的key都不存在时，设置值</h1><br>
	 * 
	 * Sets the given keys to their respective values. MSETNX will not perform any
	 * operation at all even if just a single key already exists.
	 * 
	 * Because of this semantic MSETNX can be used in order to set different keys
	 * representing different fields of a unique logic object in a way that ensures
	 * that either all the fields or none at all are set.
	 * 
	 * MSETNX is atomic, so all given keys are set at once. It is not possible for
	 * clients to see that some of the keys were updated while others are unchanged.
	 * 
	 * redis> MSETNX key1 "Hello" key2 "there"<br>
	 * (integer) 1<br>
	 * redis> MSETNX key2 "new" key3 "world"<br>
	 * (integer) 0<br>
	 * redis> MGET key1 key2 key3<br>
	 * 1) "Hello"<br>
	 * 2) "there"<br>
	 * 3) (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keysvalues
	 * @return
	 */
	Long msetnx(final byte[]... keysvalues);

	/**
	 * <h1>设置值并设置过期毫秒，key不存在则创建</h1><br>
	 * 
	 * PSETEX works exactly like SETEX with the sole difference that the expire time
	 * is specified in milliseconds instead of seconds.
	 * 
	 * redis> PSETEX mykey 1000 "Hello"<br>
	 * "OK"<br>
	 * redis> PTTL mykey<br>
	 * (integer) 1000<br>
	 * redis> GET mykey<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param milliseconds
	 * @param value
	 * @return
	 */
	String psetex(final byte[] key, final long milliseconds, final byte[] value);

	/**
	 * <h1>设置值，key不存在则创建</h1><br>
	 * 
	 * Set key to hold the string value. If key already holds a value, it is
	 * overwritten, regardless of its type. Any previous time to live associated
	 * with the key is discarded on successful SET operation.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> GET mykey<br>
	 * "Hello"<br>
	 * redis> SET anotherkey "will expire in a minute" EX 60<br>
	 * "OK"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	String set(final byte[] key, final byte[] value);

	/**
	 * <h1>设置值并设置过期秒，key不存在则创建</h1><br>
	 * 
	 * Set key to hold the string value and set key to timeout after a given number
	 * of seconds.
	 * 
	 * redis> SETEX mykey 10 "Hello"<br>
	 * "OK"<br>
	 * redis> TTL mykey<br>
	 * (integer) 10<br>
	 * redis> GET mykey<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param seconds
	 * @param value
	 * @return
	 */
	String setex(final byte[] key, final long seconds, final byte[] value);

	/**
	 * <h1>只在key不存在时设置值</h1><br>
	 * 
	 * Set key to hold string value if key does not exist. In that case, it is equal
	 * to SET. When key already holds a value, no operation is performed. SETNX is
	 * short for "SET if Not eXists".
	 * 
	 * redis> SETNX mykey "Hello"<br>
	 * (integer) 1<br>
	 * redis> SETNX mykey "World"<br>
	 * (integer) 0<br>
	 * redis> GET mykey<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	Long setnx(final byte[] key, final byte[] value);

	/**
	 * <h1>覆盖string的某段</h1><br>
	 * 
	 * Overwrites part of the string stored at key, starting at the specified
	 * offset, for the entire length of value. If the offset is larger than the
	 * current length of the string at key, the string is padded with zero-bytes to
	 * make offset fit. Non-existing keys are considered as empty strings, so this
	 * command will make sure it holds a string large enough to be able to set value
	 * at offset.
	 * 
	 * Note that the maximum offset that you can set is 2^29 -1 (536870911), as
	 * Redis Strings are limited to 512 megabytes. If you need to grow beyond this
	 * size, you can use multiple keys.
	 * 
	 * Warning: When setting the last possible byte and the string value stored at
	 * key does not yet hold a string value, or holds a small string value, Redis
	 * needs to allocate all intermediate memory which can block the server for some
	 * time. On a 2010 MacBook Pro, setting byte number 536870911 (512MB allocation)
	 * takes ~300ms, setting byte number 134217728 (128MB allocation) takes ~80ms,
	 * setting bit number 33554432 (32MB allocation) takes ~30ms and setting bit
	 * number 8388608 (8MB allocation) takes ~8ms. Note that once this first
	 * allocation is done, subsequent calls to SETRANGE for the same key will not
	 * have the allocation overhead.
	 * 
	 * 从哪里开始覆盖<br>
	 * key已存在：<br>
	 * redis> SET key1 "Hello World"<br>
	 * "OK"<br>
	 * redis> SETRANGE key1 6 "Redis"<br>
	 * (integer) 11<br>
	 * redis> GET key1<br>
	 * "Hello Redis"<br>
	 * 
	 * key不存在：<br>
	 * redis> SETRANGE key2 6 "Redis"<br>
	 * (integer) 11<br>
	 * redis> GET key2<br>
	 * "Redis"<br>
	 * 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	Long setrange(final byte[] key, final long offset, final byte[] value);

	/**
	 * <h1>获取string的长度</h1><br>
	 * 
	 * Returns the length of the string value stored at key. An error is returned
	 * when key holds a non-string value.
	 * 
	 * redis> SET mykey "Hello world"<br>
	 * "OK"<br>
	 * redis> STRLEN mykey<br>
	 * (integer) 11<br>
	 * redis> STRLEN nonexisting<br>
	 * (integer) 0<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	Long strlen(final byte[] key);

	/**
	 * <h1>返回subString部分</h1><br>
	 * 
	 * Returns the substring of the string value stored at key, determined by the
	 * offsets start and end (both are inclusive). Negative offsets can be used in
	 * order to provide an offset starting from the end of the string. So -1 means
	 * the last character, -2 the penultimate and so forth.
	 * 
	 * The function handles out of range requests by limiting the resulting range to
	 * the actual length of the string.
	 * 
	 * redis> SET mykey "This is a string"<br>
	 * "OK"<br>
	 * redis> GETRANGE mykey 0 3<br>
	 * "This"<br>
	 * redis> GETRANGE mykey -3 -1<br>
	 * "ing"<br>
	 * redis> GETRANGE mykey 0 -1<br>
	 * "This is a string"<br>
	 * redis> GETRANGE mykey 10 100<br>
	 * "string"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @return
	 */
	@Deprecated //官方：用getrange
	byte[] substr(final byte[] key, final int start, final int end);

}
