package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Set;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.redis.args.ExpiryOption;
import io.github.icodegarden.commons.redis.args.KeyScanCursor;
import io.github.icodegarden.commons.redis.args.MigrateParams;
import io.github.icodegarden.commons.redis.args.RestoreParams;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.SortArgs;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface KeyBinaryCommands {

	/**
	 * <h1>copy数据到另一个key</h1><br>
	 * 
	 * This command copies the value stored at the source key to the destination
	 * key.
	 * 
	 * By default, the destination key is created in the logical database used by
	 * the connection. The DB option allows specifying an alternative logical
	 * database index for the destination key.
	 * 
	 * The command returns an error when the destination key already exists. The
	 * REPLACE option removes the destination key before copying the value to it.
	 * 
	 * SET dolly "sheep"<br>
	 * COPY dolly clone<br>
	 * GET clone<br>
	 * 
	 * @param srcKey
	 * @param dstKey
	 * @param replace 当目标key已存在会报错，true则替换目标key
	 * @return
	 */
	boolean copy(byte[] srcKey, byte[] dstKey, boolean replace);

	long del(byte[] key);

	/**
	 * <h1>删除key</h1><br>
	 * 
	 * @param keys
	 * @return
	 */
	long del(byte[]... keys);

	/**
	 * <h1>返回被redis序列化的value</h1><br>
	 * 
	 * Serialize the value stored at key in a Redis-specific format and return it to
	 * the user. The returned value can be synthesized back into a Redis key using
	 * the RESTORE command.
	 * 
	 * The serialization format is opaque and non-standard, however it has a few
	 * semantic characteristics:
	 * 
	 * It contains a 64-bit checksum that is used to make sure errors will be
	 * detected. The RESTORE command makes sure to check the checksum before
	 * synthesizing a key using the serialized value. Values are encoded in the same
	 * format used by RDB. An RDB version is encoded inside the serialized value, so
	 * that different Redis versions with incompatible RDB formats will refuse to
	 * process the serialized value. The serialized value does NOT contain expire
	 * information. In order to capture the time to live of the current value the
	 * PTTL command should be used.
	 * 
	 * If key does not exist a nil bulk reply is returned.
	 * 
	 * > SET mykey 10<br>
	 * OK<br>
	 * > DUMP mykey<br>
	 * "\x00\xc0\n\n\x00n\x9fWE\x0e\xaec\xbb"<br>
	 * 
	 * @param key
	 * @return
	 */
	@Nullable
	byte[] dump(byte[] key);

	boolean exists(byte[] key);

	/**
	 * <h1>key是否存在</h1><br>
	 * 
	 * Returns if key exists.
	 * 
	 * The user should be aware that if the same existing key is mentioned in the
	 * arguments multiple times, it will be counted multiple times. So if somekey
	 * exists, EXISTS somekey somekey will return 2.
	 * 
	 * redis> SET key1 "Hello"<br>
	 * "OK"<br>
	 * redis> EXISTS key1<br>
	 * (integer) 1<br>
	 * redis> EXISTS nosuchkey<br>
	 * (integer) 0<br>
	 * redis> SET key2 "World"<br>
	 * "OK"<br>
	 * redis> EXISTS key1 key2 nosuchkey<br>
	 * (integer) 2<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keys
	 * @return 存在的key数量
	 */
	long exists(byte[]... keys);

	/**
	 * <h1>设置key过期时间</h1><br>
	 * 
	 * @param key
	 * @param seconds 秒
	 * @return
	 */
	long expire(byte[] key, long seconds);

	/**
	 * <h1>设置key过期时间</h1><br>
	 * 
	 * Set a timeout on key. After the timeout has expired, the key will
	 * automatically be deleted. A key with an associated timeout is often said to
	 * be volatile in Redis terminology.
	 * 
	 * The timeout will only be cleared by commands that delete or overwrite the
	 * contents of the key, including DEL, SET, GETSET and all the *STORE commands.
	 * This means that all the operations that conceptually alter the value stored
	 * at the key without replacing it with a new one will leave the timeout
	 * untouched. For instance, incrementing the value of a key with INCR, pushing a
	 * new value into a list with LPUSH, or altering the field value of a hash with
	 * HSET are all operations that will leave the timeout untouched.
	 * 
	 * The timeout can also be cleared, turning the key back into a persistent key,
	 * using the PERSIST command.
	 * 
	 * If a key is renamed with RENAME, the associated time to live is transferred
	 * to the new key name.
	 * 
	 * If a key is overwritten by RENAME, like in the case of an existing key Key_A
	 * that is overwritten by a call like RENAME Key_B Key_A, it does not matter if
	 * the original Key_A had a timeout associated or not, the new key Key_A will
	 * inherit all the characteristics of Key_B.
	 * 
	 * Note that calling EXPIRE/PEXPIRE with a non-positive timeout or
	 * EXPIREAT/PEXPIREAT with a time in the past will result in the key being
	 * deleted rather than expired (accordingly, the emitted key event will be del,
	 * not expired).
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXPIRE mykey 10<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 10<br>
	 * redis> SET mykey "Hello World"<br>
	 * "OK"<br>
	 * redis> TTL mykey<br>
	 * (integer) -1<br>
	 * redis> EXPIRE mykey 10 XX<br>
	 * (integer) 0<br>
	 * redis> TTL mykey<br>
	 * (integer) -1<br>
	 * redis> EXPIRE mykey 10 NX<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 10<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param seconds
	 * @param expiryOption
	 * @return
	 */
	long expire(byte[] key, long seconds, ExpiryOption expiryOption);

	long expireAt(byte[] key, long unixTime);

	/**
	 * <h1>设置key在什么时间过期， Unix timestamp (seconds since January 1, 1970).</h1><br>
	 * 
	 * EXPIREAT has the same effect and semantic as EXPIRE, but instead of
	 * specifying the number of seconds representing the TTL (time to live), it
	 * takes an absolute Unix timestamp (seconds since January 1, 1970). A timestamp
	 * in the past will delete the key immediately.
	 * 
	 * Please for the specific semantics of the command refer to the documentation
	 * of EXPIRE.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXISTS mykey<br>
	 * (integer) 1<br>
	 * redis> EXPIREAT mykey 1293840000<br>
	 * (integer) 1<br>
	 * redis> EXISTS mykey<br>
	 * (integer) 0<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param unixTime
	 * @param expiryOption
	 * @return
	 */
	long expireAt(byte[] key, long unixTime, ExpiryOption expiryOption);

	/**
	 * <h1>返回key的过期时间， Unix timestamp (seconds since January 1, 1970).</h1><br>
	 * 
	 * Returns the absolute Unix timestamp (since January 1, 1970) in seconds at
	 * which the given key will expire.
	 * 
	 * See also the PEXPIRETIME command which returns the same information with
	 * milliseconds resolution.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXPIREAT mykey 33177117420<br>
	 * (integer) 1<br>
	 * redis> EXPIRETIME mykey<br>
	 * (integer) 33177117420<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return Integer reply: Expiration Unix timestamp in seconds, or a negative
	 *         value in order to signal an error (see the description below). <br>
	 *         The command returns -1 if the key exists but has no associated
	 *         expiration time.<br>
	 *         The command returns -2 if the key does not exist.<br>
	 */
	long expireTime(byte[] key);

	/**
	 * <h1>返回所有符合正则的keys</h1><br>
	 * 
	 * Returns all keys matching pattern.
	 * 
	 * While the time complexity for this operation is O(N), the constant times are
	 * fairly low. For example, Redis running on an entry level laptop can scan a 1
	 * million key database in 40 milliseconds.
	 * 
	 * Warning: consider KEYS as a command that should only be used in production
	 * environments with extreme care. It may ruin performance when it is executed
	 * against large databases. This command is intended for debugging and special
	 * operations, such as changing your keyspace layout. Don't use KEYS in your
	 * regular application code. If you're looking for a way to find keys in a
	 * subset of your keyspace, consider using SCAN or sets.
	 * 
	 * Supported glob-style patterns:<br>
	 * 
	 * h?llo matches hello, hallo and hxllo<br>
	 * h*llo matches hllo and heeeello<br>
	 * h[ae]llo matches hello and hallo, but not hillo<br>
	 * h[^e]llo matches hallo, hbllo, ... but not hello<br>
	 * h[a-b]llo matches hallo and hbllo<br>
	 * Use \ to escape special characters if you want to match them verbatim.<br>
	 * 
	 * redis> MSET firstname Jack lastname Stuntman age 35<br>
	 * "OK"<br>
	 * redis> KEYS *name*<br>
	 * 1) "firstname"<br>
	 * 2) "lastname"<br>
	 * redis> KEYS a??<br>
	 * 1) "age"<br>
	 * redis> KEYS *<br>
	 * 1) "firstname"<br>
	 * 2) "age"<br>
	 * 3) "lastname"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param pattern
	 * @return
	 */
	@NotNull
	Set<byte[]> keys(byte[] pattern);

	String migrate(String host, int port, byte[] key, int timeout);

	/**
	 * <h1>把key迁移到另一个实例</h1><br>
	 * 
	 * Atomically transfer a key from a source Redis instance to a destination Redis
	 * instance. On success the key is deleted from the original instance and is
	 * guaranteed to exist in the target instance.
	 * 
	 * The command is atomic and blocks the two instances for the time required to
	 * transfer the key, at any given time the key will appear to exist in a given
	 * instance or in the other instance, unless a timeout error occurs. In 3.2 and
	 * above, multiple keys can be pipelined in a single call to MIGRATE by passing
	 * the empty string ("") as key and adding the KEYS clause.
	 * 
	 * The command internally uses DUMP to generate the serialized version of the
	 * key value, and RESTORE in order to synthesize the key in the target instance.
	 * The source instance acts as a client for the target instance. If the target
	 * instance returns OK to the RESTORE command, the source instance deletes the
	 * key using DEL.
	 * 
	 * The timeout specifies the maximum idle time in any moment of the
	 * communication with the destination instance in milliseconds. This means that
	 * the operation does not need to be completed within the specified amount of
	 * milliseconds, but that the transfer should make progresses without blocking
	 * for more than the specified amount of milliseconds.
	 * 
	 * MIGRATE needs to perform I/O operations and to honor the specified timeout.
	 * When there is an I/O error during the transfer or if the timeout is reached
	 * the operation is aborted and the special error - IOERR returned. When this
	 * happens the following two cases are possible:
	 * 
	 * The key may be on both the instances. The key may be only in the source
	 * instance. It is not possible for the key to get lost in the event of a
	 * timeout, but the client calling MIGRATE, in the event of a timeout error,
	 * should check if the key is also present in the target instance and act
	 * accordingly.
	 * 
	 * When any other error is returned (starting with ERR) MIGRATE guarantees that
	 * the key is still only present in the originating instance (unless a key with
	 * the same name was also already present on the target instance).
	 * 
	 * If there are no keys to migrate in the source instance NOKEY is returned.
	 * Because missing keys are possible in normal conditions, from expiry for
	 * example, NOKEY isn't an error.
	 * 
	 * 
	 * 
	 * @param host
	 * @param port
	 * @param timeout
	 * @param params
	 * @param keys
	 * @return Simple string reply: The command returns OK on success, or NOKEY if
	 *         no keys were found in the source instance.
	 */
	String migrate(String host, int port, int timeout, MigrateParams params, byte[]... keys);

	/**
	 * <h1>返回redis内部对object的encoding</h1><br>
	 * 
	 * Returns the internal encoding for the Redis object stored at <key>
	 * 
	 * Redis objects can be encoded in different ways:
	 * 
	 * Strings can be encoded as:
	 * 
	 * raw, normal string encoding. int, strings representing integers in a 64-bit
	 * signed interval, encoded in this way to save space. embstr, an embedded
	 * string, which is an object where the internal simple dynamic string, sds, is
	 * an unmodifiable string allocated in the same chuck as the object itself.
	 * embstr can be strings with lengths up to the hardcoded limit of
	 * OBJ_ENCODING_EMBSTR_SIZE_LIMIT or 44 bytes. Lists can be encoded as ziplist
	 * or linkedlist. The ziplist is the special representation that is used to save
	 * space for small lists.
	 * 
	 * Sets can be encoded as intset or hashtable. The intset is a special encoding
	 * used for small sets composed solely of integers.
	 * 
	 * Hashes can be encoded as ziplist or hashtable. The ziplist is a special
	 * encoding used for small hashes.
	 * 
	 * Sorted Sets can be encoded as ziplist or skiplist format. As for the List
	 * type small sorted sets can be specially encoded using ziplist, while the
	 * skiplist encoding is the one that works with sorted sets of any size.
	 * 
	 * All the specially encoded types are automatically converted to the general
	 * type once you perform an operation that makes it impossible for Redis to
	 * retain the space saving encoding.
	 * 
	 * 
	 * 
	 * @param key
	 * @return Bulk string reply: the encoding of the object, or nil if the key
	 *         doesn't exist
	 */
	@Nullable
	byte[] objectEncoding(byte[] key);

	/**
	 * <h1>Returns the logarithmic access frequency counter of a Redis
	 * object.</h1><br>
	 * 
	 * This command returns the logarithmic access frequency counter of a Redis
	 * object stored at <key>.
	 * 
	 * The command is only available when the maxmemory-policy configuration
	 * directive is set to one of the LFU policies.
	 * 
	 * @param key
	 * @return
	 */
	Long objectFreq(byte[] key);

	/**
	 * <h1>Returns the time since the last access to a Redis object.</h1><br>
	 * 
	 * This command returns the time in seconds since the last access to the value
	 * stored at <key>.
	 * 
	 * The command is only available when the maxmemory-policy configuration
	 * directive is not set to one of the LFU policies.
	 * 
	 * @param key
	 * @return
	 */
	Long objectIdletime(byte[] key);

	/**
	 * <h1>Returns the reference count of a value of a key.</h1><br>
	 * 
	 * This command returns the reference count of the stored at <key>.
	 * 
	 * @param key
	 * @return
	 */
	Long objectRefcount(byte[] key);

	/**
	 * <h1>移除过期时间，转为永久</h1><br>
	 * 
	 * Remove the existing timeout on key, turning the key from volatile (a key with
	 * an expire set) to persistent (a key that will never expire as no timeout is
	 * associated).
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXPIRE mykey 10<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 10<br>
	 * redis> PERSIST mykey<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) -1<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	long persist(byte[] key);

	long pexpire(byte[] key, long milliseconds);

	/**
	 * <h1>设置过期时间，毫秒</h1><br>
	 * 
	 * This command works exactly like EXPIRE but the time to live of the key is
	 * specified in milliseconds instead of seconds.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> PEXPIRE mykey 1500<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 1<br>
	 * redis> PTTL mykey<br>
	 * (integer) 1499<br>
	 * redis> PEXPIRE mykey 1000 XX<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 1<br>
	 * redis> PEXPIRE mykey 1000 NX<br>
	 * (integer) 0<br>
	 * redis> TTL mykey<br>
	 * (integer) 1<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param milliseconds
	 * @param expiryOption
	 * @return
	 */
	long pexpire(byte[] key, long milliseconds, ExpiryOption expiryOption);

	long pexpireAt(byte[] key, long millisecondsTimestamp);

	/**
	 * <h1>设置key在什么时间过期，毫秒</h1><br>
	 * 
	 * PEXPIREAT has the same effect and semantic as EXPIREAT, but the Unix time at
	 * which the key will expire is specified in milliseconds instead of seconds.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> PEXPIREAT mykey 1555555555005<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) -2<br>
	 * redis> PTTL mykey<br>
	 * (integer) -2<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param millisecondsTimestamp
	 * @param expiryOption
	 * @return
	 */
	long pexpireAt(byte[] key, long millisecondsTimestamp, ExpiryOption expiryOption);

	/**
	 * <h1>返回key的过期时间，毫秒</h1><br>
	 * 
	 * PEXPIRETIME has the same semantic as EXPIRETIME, but returns the absolute
	 * Unix expiration timestamp in milliseconds instead of seconds.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> PEXPIREAT mykey 33177117420000<br>
	 * (integer) 1<br>
	 * redis> PEXPIRETIME mykey<br>
	 * (integer) 33177117420000<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	long pexpireTime(byte[] key);

	/**
	 * <h1>返回key的剩余过期时间，毫秒</h1><br>
	 * 
	 * Like TTL this command returns the remaining time to live of a key that has an
	 * expire set, with the sole difference that TTL returns the amount of remaining
	 * time in seconds while PTTL returns it in milliseconds.
	 * 
	 * In Redis 2.6 or older the command returns -1 if the key does not exist or if
	 * the key exist but has no associated expire.
	 * 
	 * Starting with Redis 2.8 the return value in case of error changed:
	 * 
	 * The command returns -2 if the key does not exist. The command returns -1 if
	 * the key exists but has no associated expire.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXPIRE mykey 1<br>
	 * (integer) 1<br>
	 * redis> PTTL mykey<br>
	 * (integer) 999<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	long pttl(byte[] key);

	/**
	 * <h1>在选中的database中随机返回一个key，如果里面没有key返回null</h1><br>
	 * 
	 * Return a random key from the currently selected database.
	 * 
	 * 
	 * @return Bulk string reply: the random key, or nil when the database is empty.
	 */
	@Nullable
	byte[] randomBinaryKey();

	/**
	 * <h1>重命名并覆盖已存在的key，如果key不存在则异常</h1><br>
	 * 
	 * Renames key to newkey. It returns an error when key does not exist. If newkey
	 * already exists it is overwritten, when this happens RENAME executes an
	 * implicit DEL operation, so if the deleted key contains a very big value it
	 * may cause high latency even if RENAME itself is usually a constant-time
	 * operation.
	 * 
	 * In Cluster mode, both key and newkey must be in the same hash slot, meaning
	 * that in practice only keys that have the same hash tag can be reliably
	 * renamed in cluster.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> RENAME mykey myotherkey<br>
	 * "OK"<br>
	 * redis> GET myotherkey<br>
	 * "Hello"<br>
	 * redis> <br>
	 * 
	 * @param oldkey
	 * @param newkey
	 * @return
	 */
	String rename(byte[] oldkey, byte[] newkey);

	/**
	 * <h1>当目标key不存在时重命名key，如果key不存在则异常</h1><br>
	 * 
	 * Renames key to newkey if newkey does not yet exist. It returns an error when
	 * key does not exist.
	 * 
	 * In Cluster mode, both key and newkey must be in the same hash slot, meaning
	 * that in practice only keys that have the same hash tag can be reliably
	 * renamed in cluster.
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> SET myotherkey "World"<br>
	 * "OK"<br>
	 * redis> RENAMENX mykey myotherkey<br>
	 * (integer) 0<br>
	 * redis> GET myotherkey<br>
	 * "World"<br>
	 * redis> <br>
	 * 
	 * @param oldkey
	 * @param newkey
	 * @return
	 */
	long renamenx(byte[] oldkey, byte[] newkey);

	String restore(byte[] key, long ttl, byte[] serializedValue);

	/**
	 * <h1>直接存储序列化的值，例如list</h1><br>
	 * 
	 * Create a key associated with a value that is obtained by deserializing the
	 * provided serialized value (obtained via DUMP).
	 * 
	 * If ttl is 0 the key is created without any expire, otherwise the specified
	 * expire time (in milliseconds) is set.
	 * 
	 * If the ABSTTL modifier was used, ttl should represent an absolute Unix
	 * timestamp (in milliseconds) in which the key will expire.
	 * 
	 * For eviction purposes, you may use the IDLETIME or FREQ modifiers. See OBJECT
	 * for more information.
	 * 
	 * RESTORE will return a "Target key name is busy" error when key already exists
	 * unless you use the REPLACE modifier.
	 * 
	 * RESTORE checks the RDB version and data checksum. If they don't match an
	 * error is returned.
	 * 
	 * redis> DEL mykey<br>
	 * 0<br>
	 * redis> RESTORE mykey 0 "\n\x17\x17\x00\x00\x00\x12\x00\x00\x00\x03\x00\<br>
	 * x00\xc0\x01\x00\x04\xc0\x02\x00\x04\xc0\x03\x00\<br>
	 * xff\x04\x00u#<\xc0;.\xe9\xdd"<br>
	 * OK<br>
	 * redis> TYPE mykey<br>
	 * list<br>
	 * redis> LRANGE mykey 0 -1<br>
	 * 1) "1"<br>
	 * 2) "2"<br>
	 * 3) "3"<br>
	 * 
	 * @param key
	 * @param ttl             0表示不过期
	 * @param serializedValue
	 * @param params
	 * @return
	 */
	String restore(byte[] key, long ttl, byte[] serializedValue, RestoreParams params);

	KeyScanCursor<byte[]> scan(byte[] cursor);

	KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params);

	/**
	 * <h1>遍历所有key</h1><br>
	 * cursor 0 作为遍历的开始和结束
	 * 
	 * The SCAN command and the closely related commands SSCAN, HSCAN and ZSCAN are
	 * used in order to incrementally iterate over a collection of elements.
	 * 
	 * SCAN iterates the set of keys in the currently selected Redis database. SSCAN
	 * iterates elements of Sets types. HSCAN iterates fields of Hash types and
	 * their associated values. ZSCAN iterates elements of Sorted Set types and
	 * their associated scores. Since these commands allow for incremental
	 * iteration, returning only a small number of elements per call, they can be
	 * used in production without the downside of commands like KEYS or SMEMBERS
	 * that may block the server for a long time (even several seconds) when called
	 * against big collections of keys or elements.
	 * 
	 * However while blocking commands like SMEMBERS are able to provide all the
	 * elements that are part of a Set in a given moment, The SCAN family of
	 * commands only offer limited guarantees about the returned elements since the
	 * collection that we incrementally iterate can change during the iteration
	 * process.
	 * 
	 * Note that SCAN, SSCAN, HSCAN and ZSCAN all work very similarly, so this
	 * documentation covers all the four commands. However an obvious difference is
	 * that in the case of SSCAN, HSCAN and ZSCAN the first argument is the name of
	 * the key holding the Set, Hash or Sorted Set value. The SCAN command does not
	 * need any key name argument as it iterates keys in the current database, so
	 * the iterated object is the database itself.
	 * 
	 * redis 127.0.0.1:6379> scan 0<br>
	 * 1) "17"<br>
	 * 2) 1) "key:12"<br>
	 * 2) "key:8"<br>
	 * 3) "key:4"<br>
	 * 4) "key:14"<br>
	 * 5) "key:16"<br>
	 * 6) "key:17"<br>
	 * 7) "key:15"<br>
	 * 8) "key:10"<br>
	 * 9) "key:3"<br>
	 * 10) "key:7"<br>
	 * 11) "key:1"<br>
	 * redis 127.0.0.1:6379> scan 17<br>
	 * 1) "0"<br>
	 * 2) 1) "key:5"<br>
	 * 2) "key:18"<br>
	 * 3) "key:0"<br>
	 * 4) "key:2"<br>
	 * 5) "key:19"<br>
	 * 6) "key:13"<br>
	 * 7) "key:6"<br>
	 * 8) "key:9"<br>
	 * 9) "key:11"<br>
	 * 
	 * @param cursor
	 * @param params
	 * @param type
	 * @return cursor=0表示结束
	 */
	KeyScanCursor<byte[]> scan(byte[] cursor, ScanArgs params, byte[] type);

	List<byte[]> sort(byte[] key);

	List<byte[]> sort(byte[] key, SortArgs params);

	long sort(byte[] key, byte[] dstkey);

	/**
	 * <h1>排序元素，list、set、sorted set</h1><br>
	 * 
	 * SORT mylist<br>
	 * SORT mylist DESC<br>
	 * SORT mylist ALPHA<br>
	 * SORT mylist LIMIT 0 10<br>
	 * SORT mylist LIMIT 0 5 ALPHA DESC<br>
	 * SORT mylist BY weight_*<br>
	 * SORT mylist BY nosort<br>
	 * SORT mylist BY weight_* GET object_*<br>
	 * SORT mylist BY weight_* GET object_* GET #<br>
	 * SORT mylist BY weight_* STORE resultkey<br>
	 * SORT mylist BY weight_*->fieldname GET object_*->fieldname<br>
	 * 
	 * 
	 * @param key
	 * @param sortingParams
	 * @param dstkey
	 * @return
	 */
	long sort(byte[] key, SortArgs params, byte[] dstkey);

	/**
	 * <h1>返回排序结果，list、set、sorted set</h1><br>
	 * SORT_RO key [BY pattern] [LIMIT offset count] [GET pattern [GET
  pattern ...]] [ASC | DESC] [ALPHA]<br>
  
  SORT_RO mylist BY weight_*->fieldname GET object_*->fieldname<br>
  
	 * @param key
	 * @param sortingParams
	 * @return
	 */
	List<byte[]> sortReadonly(byte[] key, SortArgs params);

	long touch(byte[] key);

	/**
	 * <h1>Returns the number of existing keys out of those specified after updating
	 * the time they were last accessed.</h1><br>
	 * 
	 * redis> SET key1 "Hello" "OK" redis> SET key2 "World" "OK" redis> TOUCH key1
	 * key2 (integer) 2 redis>
	 * 
	 * 
	 * @param keys
	 * @return
	 */
	long touch(byte[]... keys);

	/**
	 * <h1>返回剩余过期时间，秒</h1><br>
	 * 
	 * Returns the remaining time to live of a key that has a timeout. This
	 * introspection capability allows a Redis client to check how many seconds a
	 * given key will continue to be part of the dataset.
	 * 
	 * In Redis 2.6 or older the command returns -1 if the key does not exist or if
	 * the key exist but has no associated expire.
	 * 
	 * Starting with Redis 2.8 the return value in case of error changed:
	 * 
	 * The command returns -2 if the key does not exist. The command returns -1 if
	 * the key exists but has no associated expire. See also the PTTL command that
	 * returns the same information with milliseconds resolution (Only available in
	 * Redis 2.6 or greater).
	 * 
	 * redis> SET mykey "Hello"<br>
	 * "OK"<br>
	 * redis> EXPIRE mykey 10<br>
	 * (integer) 1<br>
	 * redis> TTL mykey<br>
	 * (integer) 10<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return
	 */
	long ttl(byte[] key);

	/**
	 * <h1>返回key对应的数据类型</h1><br>
	 * 
	 * Returns the string representation of the type of the value stored at key. The
	 * different types that can be returned are: string, list, set, zset, hash and
	 * stream.
	 * 
	 * redis> SET key1 "value"<br>
	 * "OK"<br>
	 * redis> LPUSH key2 "value"<br>
	 * (integer) 1<br>
	 * redis> SADD key3 "value"<br>
	 * (integer) 1<br>
	 * redis> TYPE key1<br>
	 * "string"<br>
	 * redis> TYPE key2<br>
	 * "list"<br>
	 * redis> TYPE key3<br>
	 * "set"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @return
	 */
	String type(byte[] key);

	long unlink(byte[] key);

	/**
	 * <h1>异步删除key</h1><br>
	 * 
	 * This command is very similar to DEL: it removes the specified keys. Just like
	 * DEL a key is ignored if it does not exist. However the command performs the
	 * actual memory reclaiming in a different thread, so it is not blocking, while
	 * DEL is. This is where the command name comes from: the command just unlinks
	 * the keys from the keyspace. The actual removal will happen later
	 * asynchronously.
	 * 
	 * redis> SET key1 "Hello"<br>
	 * "OK"<br>
	 * redis> SET key2 "World"<br>
	 * "OK"<br>
	 * redis> UNLINK key1 key2 key3<br>
	 * (integer) 2<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keys
	 * @return
	 */
	long unlink(byte[]... keys);

	@Deprecated //lettuce返回null
	Long memoryUsage(byte[] key);
	@Deprecated //lettuce返回null
	Long memoryUsage(byte[] key, int samples);

}