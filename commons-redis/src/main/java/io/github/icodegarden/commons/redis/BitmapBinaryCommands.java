package io.github.icodegarden.commons.redis;

import java.util.List;

import redis.clients.jedis.args.BitCountOption;
import redis.clients.jedis.args.BitOP;
import redis.clients.jedis.params.BitPosParams;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface BitmapBinaryCommands {
	long bitcount(byte[] key);

	long bitcount(byte[] key, long start, long end);

	/**
	 * <h1>Count the number of set bits (population counting) in a string.</h1><br>
	 * BITCOUNT key [start end [BYTE | BIT]]
	 * 
	 * Count the number of set bits (population counting) in a string.
	 * 
	 * By default all the bytes contained in the string are examined. It is possible
	 * to specify the counting operation only in an interval passing the additional
	 * arguments start and end.
	 * 
	 * Like for the GETRANGE command start and end can contain negative values in
	 * order to index bytes starting from the end of the string, where -1 is the
	 * last byte, -2 is the penultimate, and so forth.
	 * 
	 * Non-existent keys are treated as empty strings, so the command will return
	 * zero.
	 * 
	 * By default, the additional arguments start and end specify a byte index. We
	 * can use an additional argument BIT to specify a bit index. So 0 is the first
	 * bit, 1 is the second bit, and so forth. For negative values, -1 is the last
	 * bit, -2 is the penultimate, and so forth.
	 * 
	 * redis> SET mykey "foobar"<br>
	 * "OK"<br>
	 * redis> BITCOUNT mykey<br>
	 * (integer) 26<br>
	 * redis> BITCOUNT mykey 0 0<br>
	 * (integer) 4<br>
	 * redis> BITCOUNT mykey 1 1<br>
	 * (integer) 6<br>
	 * redis> BITCOUNT mykey 1 1 BYTE<br>
	 * (integer) 6<br>
	 * redis> BITCOUNT mykey 5 30 BIT<br>
	 * (integer) 17<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param start
	 * @param end
	 * @param option
	 * @return
	 */
	long bitcount(byte[] key, long start, long end, BitCountOption option);

	/**
	 * <h1>Performs arbitrary bitfield integer operations on strings.</h1><br>
	 * BITFIELD key [GET encoding offset | [OVERFLOW <WRAP | SAT | FAIL>]<br>
	 * <SET encoding offset value | INCRBY encoding offset increment><br>
	 * [GET encoding offset | [OVERFLOW <WRAP | SAT | FAIL>]<br>
	 * <SET encoding offset value | INCRBY encoding offset increment><br>
	 * ...]]<br>
	 * 
	 * The command treats a Redis string as an array of bits, and is capable of
	 * addressing specific integer fields of varying bit widths and arbitrary non
	 * (necessary) aligned offset. In practical terms using this command you can
	 * set, for example, a signed 5 bits integer at bit offset 1234 to a specific
	 * value, retrieve a 31 bit unsigned integer from offset 4567. Similarly the
	 * command handles increments and decrements of the specified integers,
	 * providing guaranteed and well specified overflow and underflow behavior that
	 * the user can configure.
	 * 
	 * BITFIELD is able to operate with multiple bit fields in the same command
	 * call. It takes a list of operations to perform, and returns an array of
	 * replies, where each array matches the corresponding operation in the list of
	 * arguments.
	 * 
	 * For example the following command increments a 5 bit signed integer at bit
	 * offset 100, and gets the value of the 4 bit unsigned integer at bit offset 0:
	 * 
	 * > BITFIELD mykey INCRBY i5 100 1 GET u4 0<br>
	 * 1) (integer) 1<br>
	 * 2) (integer) 0<br>
	 * 
	 * @param key
	 * @param arguments
	 * @return
	 */
	List<Long> bitfield(final byte[] key, final byte[]... arguments);

	/**
	 * <h1>Performs arbitrary read-only bitfield integer operations on
	 * strings.</h1><br>
	 * BITFIELD_RO key [GET encoding offset [GET encoding offset ...]]
	 * 
	 * Read-only variant of the BITFIELD command. It is like the original BITFIELD
	 * but only accepts GET subcommand and can safely be used in read-only replicas.
	 * 
	 * Since the original BITFIELD has SET and INCRBY options it is technically
	 * flagged as a writing command in the Redis command table. For this reason
	 * read-only replicas in a Redis Cluster will redirect it to the master instance
	 * even if the connection is in read-only mode (see the READONLY command of
	 * Redis Cluster).
	 * 
	 * Since Redis 6.2, the BITFIELD_RO variant was introduced in order to allow
	 * BITFIELD behavior in read-only replicas without breaking compatibility on
	 * command flags.
	 * 
	 * See original BITFIELD for more details.
	 * 
	 * BITFIELD_RO hello GET i8 16
	 * 
	 * @param key
	 * @param arguments
	 * @return
	 */
	List<Long> bitfieldReadonly(byte[] key, final byte[]... arguments);

	/**
	 * <h1>Performs bitwise operations on multiple strings, and stores the
	 * result.</h1><br>
	 * BITOP <AND | OR | XOR | NOT> destkey key [key ...]
	 * 
	 * 
	 * Perform a bitwise operation between multiple keys (containing string values)
	 * and store the result in the destination key.
	 * 
	 * The BITOP command supports four bitwise operations: AND, OR, XOR and NOT,
	 * thus the valid forms to call the command are:
	 * 
	 * BITOP AND destkey srckey1 srckey2 srckey3 ... srckeyN BITOP OR destkey
	 * srckey1 srckey2 srckey3 ... srckeyN BITOP XOR destkey srckey1 srckey2 srckey3
	 * ... srckeyN BITOP NOT destkey srckey As you can see NOT is special as it only
	 * takes an input key, because it performs inversion of bits so it only makes
	 * sense as a unary operator.
	 * 
	 * The result of the operation is always stored at destkey.
	 * 
	 * redis> SET key1 "foobar"<br>
	 * "OK"<br>
	 * redis> SET key2 "abcdef"<br>
	 * "OK"<br>
	 * redis> BITOP AND dest key1 key2<br>
	 * (integer) 6<br>
	 * redis> GET dest<br>
	 * "`bc`ab"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param op
	 * @param destKey
	 * @param srcKeys
	 * @return
	 */
	long bitop(final BitOP op, final byte[] destKey, final byte[]... srcKeys);

	long bitpos(final byte[] key, final boolean value);

	/**
	 * <h1>Finds the first set (1) or clear (0) bit in a string.</h1><br>
	 * BITPOS key bit [start [end [BYTE | BIT]]]
	 * 
	 * Return the position of the first bit set to 1 or 0 in a string.
	 * 
	 * The position is returned, thinking of the string as an array of bits from
	 * left to right, where the first byte's most significant bit is at position 0,
	 * the second byte's most significant bit is at position 8, and so forth.
	 * 
	 * The same bit position convention is followed by GETBIT and SETBIT.
	 * 
	 * By default, all the bytes contained in the string are examined. It is
	 * possible to look for bits only in a specified interval passing the additional
	 * arguments start and end (it is possible to just pass start, the operation
	 * will assume that the end is the last byte of the string. However there are
	 * semantic differences as explained later). By default, the range is
	 * interpreted as a range of bytes and not a range of bits, so start=0 and end=2
	 * means to look at the first three bytes.
	 * 
	 * You can use the optional BIT modifier to specify that the range should be
	 * interpreted as a range of bits. So start=0 and end=2 means to look at the
	 * first three bits.
	 * 
	 * Note that bit positions are returned always as absolute values starting from
	 * bit zero even when start and end are used to specify a range.
	 * 
	 * Like for the GETRANGE command start and end can contain negative values in
	 * order to index bytes starting from the end of the string, where -1 is the
	 * last byte, -2 is the penultimate, and so forth. When BIT is specified, -1 is
	 * the last bit, -2 is the penultimate, and so forth.
	 * 
	 * Non-existent keys are treated as empty strings.
	 * 
	 * redis> SET mykey "\xff\xf0\x00"<br>
	 * "OK"<br>
	 * redis> BITPOS mykey 0<br>
	 * (integer) 0<br>
	 * redis> SET mykey "\x00\xff\xf0"<br>
	 * "OK"<br>
	 * redis> BITPOS mykey 1 0<br>
	 * (integer) 1<br>
	 * redis> BITPOS mykey 1 2<br>
	 * (integer) 18<br>
	 * redis> BITPOS mykey 1 2 -1 BYTE<br>
	 * (integer) 18<br>
	 * redis> BITPOS mykey 1 7 15 BIT<br>
	 * (integer) 9<br>
	 * redis> set mykey "\x00\x00\x00"<br>
	 * "OK"<br>
	 * redis> BITPOS mykey 1<br>
	 * (integer) 1<br>
	 * redis> BITPOS mykey 1 7 -3 BIT<br>
	 * (integer) 9<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param value
	 * @param params
	 * @return
	 */
	long bitpos(final byte[] key, final boolean value, final BitPosParams params);

	/**
	 * <h1>返回对应的bit位值</h1><br>
	 * GETBIT key offset
	 * 
	 * Returns the bit value at offset in the string value stored at key.
	 * 
	 * When offset is beyond the string length, the string is assumed to be a
	 * contiguous space with 0 bits. When key does not exist it is assumed to be an
	 * empty string, so offset is always out of range and the value is also assumed
	 * to be a contiguous space with 0 bits.
	 * 
	 * redis> SETBIT mykey 7 1<br>
	 * (integer) 0<br>
	 * redis> GETBIT mykey 0<br>
	 * (integer) 0<br>
	 * redis> GETBIT mykey 7<br>
	 * (integer) 1<br>
	 * redis> GETBIT mykey 100<br>
	 * (integer) 0<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param offset
	 * @return
	 */
	boolean getbit(final byte[] key, final long offset);

	/**
	 * <h1>设置对应的bit位值</h1><br>
	 * SETBIT key offset value
	 * 
	 * Sets or clears the bit at offset in the string value stored at key.
	 * 
	 * The bit is either set or cleared depending on value, which can be either 0 or
	 * 1.
	 * 
	 * When key does not exist, a new string value is created. The string is grown
	 * to make sure it can hold a bit at offset. The offset argument is required to
	 * be greater than or equal to 0, and smaller than 2^32 (this limits bitmaps to
	 * 512MB). When the string at key is grown, added bits are set to 0.
	 * 
	 * Warning: When setting the last possible bit (offset equal to 2^32 -1) and the
	 * string value stored at key does not yet hold a string value, or holds a small
	 * string value, Redis needs to allocate all intermediate memory which can block
	 * the server for some time. On a 2010 MacBook Pro, setting bit number 2^32 -1
	 * (512MB allocation) takes ~300ms, setting bit number 2^30 -1 (128MB
	 * allocation) takes ~80ms, setting bit number 2^28 -1 (32MB allocation) takes
	 * ~30ms and setting bit number 2^26 -1 (8MB allocation) takes ~8ms. Note that
	 * once this first allocation is done, subsequent calls to SETBIT for the same
	 * key will not have the allocation overhead.
	 * 
	 * redis> SETBIT mykey 7 1<br>
	 * (integer) 0<br>
	 * redis> SETBIT mykey 7 0<br>
	 * (integer) 1<br>
	 * redis> GET mykey<br>
	 * ""<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param offset
	 * @param value
	 * @return
	 */
	boolean setbit(final byte[] key, final long offset, final boolean value);
}