//package io.github.icodegarden.commons.redis;
//
//import java.util.List;
//import java.util.Set;
//
//import redis.clients.jedis.params.ScanParams;
//import redis.clients.jedis.resps.ScanResult;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public interface SetBinaryCommands {
//
//	/**
//	 * <h1>增加1个或多个元素</h1><br>
//	 * 
//	 * Add the specified members to the set stored at key. Specified members that
//	 * are already a member of this set are ignored. If key does not exist, a new
//	 * set is created before adding the specified members.
//	 * 
//	 * redis> SADD myset "Hello"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "World"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "World"<br>
//	 * (integer) 0<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "World"<br>
//	 * 2) "Hello"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param members
//	 * @return
//	 */
//	Long sadd(final byte[] key, final byte[]... members);
//
//	/**
//	 * <h1>返回元素的个数</h1><br>
//	 * 
//	 * Returns the set cardinality (number of elements) of the set stored at key.
//	 * 
//	 * redis> SADD myset "Hello"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "World"<br>
//	 * (integer) 1<br>
//	 * redis> SCARD myset<br>
//	 * (integer) 2<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @return
//	 */
//	Long scard(final byte[] key);
//
//	/**
//	 * <h1>返回多个set之间的差集</h1><br>
//	 * 
//	 * Returns the members of the set resulting from the difference between the
//	 * first set and all the successive sets.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SDIFF key1 key2<br>
//	 * 1) "a"<br>
//	 * 2) "b"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param keys
//	 * @return
//	 */
//	Set<byte[]> sdiff(final byte[]... keys);
//
//	/**
//	 * <h1>另存为多个set之间的差集</h1><br>
//	 * 
//	 * This command is equal to SDIFF, but instead of returning the resulting set,
//	 * it is stored in destination.
//	 * 
//	 * If destination already exists, it is overwritten.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SDIFFSTORE key key1 key2<br>
//	 * (integer) 2<br>
//	 * redis> SMEMBERS key<br>
//	 * 1) "a"<br>
//	 * 2) "b"<br>
//	 * redis> <br>
//	 * 
//	 * @param dstkey
//	 * @param keys
//	 * @return
//	 */
//	Long sdiffstore(final byte[] dstkey, final byte[]... keys);
//
//	/**
//	 * <h1>返回多个set之间的交集</h1><br>
//	 * 
//	 * Returns the members of the set resulting from the intersection of all the
//	 * given sets.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SINTER key1 key2<br>
//	 * 1) "c"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param keys
//	 * @return
//	 */
//	Set<byte[]> sinter(final byte[]... keys);
//
//	long sintercard(byte[]... keys);
//
//	/**
//	 * <h1>返回多个set之间的交集的元素个数</h1><br>
//	 * 
//	 * This command is similar to SINTER, but instead of returning the result set,
//	 * it returns just the cardinality of the result. Returns the cardinality of the
//	 * set which would result from the intersection of all the given sets.
//	 * 
//	 * Keys that do not exist are considered to be empty sets. With one of the keys
//	 * being an empty set, the resulting set is also empty (since set intersection
//	 * with an empty set always results in an empty set).
//	 * 
//	 * By default, the command calculates the cardinality of the intersection of all
//	 * given sets. When provided with the optional LIMIT argument (which defaults to
//	 * 0 and means unlimited), if the intersection cardinality reaches limit partway
//	 * through the computation, the algorithm will exit and yield limit as the
//	 * cardinality. Such implementation ensures a significant speedup for queries
//	 * where the limit is lower than the actual intersection cardinality.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SINTER key1 key2<br>
//	 * 1) "c"<br>
//	 * 2) "d"<br>
//	 * redis> SINTERCARD 2 key1 key2<br>
//	 * (integer) 2<br>
//	 * redis> SINTERCARD 2 key1 key2 LIMIT 1<br>
//	 * (integer) 1<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param limit 最大统计值，0表示不限制
//	 * @param keys
//	 * @return
//	 */
//	long sintercard(int limit, byte[]... keys);
//
//	/**
//	 * <h1>另存为多个set之间的交集</h1><br>
//	 * 
//	 * This command is equal to SINTER, but instead of returning the resulting set,
//	 * it is stored in destination.
//	 * 
//	 * If destination already exists, it is overwritten.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SINTERSTORE key key1 key2<br>
//	 * (integer) 1<br>
//	 * redis> SMEMBERS key<br>
//	 * 1) "c"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param dstkey
//	 * @param keys
//	 * @return
//	 */
//	Long sinterstore(final byte[] dstkey, final byte[]... keys);
//
//	/**
//	 * <h1>元素是否在set中</h1><br>
//	 * 
//	 * Returns if member is a member of the set stored at key.
//	 * 
//	 * redis> SADD myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SISMEMBER myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SISMEMBER myset "two"<br>
//	 * (integer) 0<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param member
//	 * @return
//	 */
//	Boolean sismember(final byte[] key, final byte[] member);
//
//	/**
//	 * <h1>获取所有元素</h1><br>
//	 * 
//	 * Returns all the members of the set value stored at key.
//	 * 
//	 * This has the same effect as running SINTER with one argument key.
//	 * 
//	 * redis> SADD myset "Hello"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "World"<br>
//	 * (integer) 1<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "World"<br>
//	 * 2) "Hello"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @return
//	 */
//	Set<byte[]> smembers(final byte[] key);
//
//	/**
//	 * <h1>多个元素是否在set中</h1><br>
//	 * 
//	 * Returns whether each member is a member of the set stored at key.
//	 * 
//	 * For every member, 1 is returned if the value is a member of the set, or 0 if
//	 * the element is not a member of the set or if key does not exist. <br>
//	 * 
//	 * redis> SADD myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "one"<br>
//	 * (integer) 0<br>
//	 * redis> SMISMEMBER myset "one" "notamember"<br>
//	 * 1) (integer) 1<br>
//	 * 2) (integer) 0<br>
//	 * 
//	 * @param key
//	 * @param members
//	 * @return
//	 */
//	List<Boolean> smismember(final byte[] key, final byte[]... members);
//
//	/**
//	 * <h1>把元素移到另一个set中</h1><br>
//	 * 
//	 * Move member from the set at source to the set at destination. This operation
//	 * is atomic. In every given moment the element will appear to be a member of
//	 * source or destination for other clients.
//	 * 
//	 * If the source set does not exist or does not contain the specified element,
//	 * no operation is performed and 0 is returned. Otherwise, the element is
//	 * removed from the source set and added to the destination set. When the
//	 * specified element already exists in the destination set, it is only removed
//	 * from the source set.
//	 * 
//	 * An error is returned if source or destination does not hold a set value. <br>
//	 * redis> SADD myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "two"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myotherset "three"<br>
//	 * (integer) 1<br>
//	 * redis> SMOVE myset myotherset "two"<br>
//	 * (integer) 1<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "one"<br>
//	 * redis> SMEMBERS myotherset<br>
//	 * 1) "three"<br>
//	 * 2) "two"<br>
//	 * redis> <br>
//	 * 
//	 * @param srckey
//	 * @param dstkey
//	 * @param member
//	 * @return
//	 */
//	Long smove(final byte[] srckey, final byte[] dstkey, final byte[] member);
//
//	byte[] spop(final byte[] key);
//
//	/**
//	 * <h1>随机pop一个或多个元素</h1><br>
//	 * 
//	 * Removes and returns one or more random members from the set value store at
//	 * key.
//	 * 
//	 * This operation is similar to SRANDMEMBER, that returns one or more random
//	 * elements from a set but does not remove it.
//	 * 
//	 * By default, the command pops a single member from the set. When provided with
//	 * the optional count argument, the reply will consist of up to count members,
//	 * depending on the set's cardinality. <br>
//	 * redis> SADD myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "two"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "three"<br>
//	 * (integer) 1<br>
//	 * redis> SPOP myset<br>
//	 * "two"<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "three"<br>
//	 * 2) "one"<br>
//	 * redis> SADD myset "four"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "five"<br>
//	 * (integer) 1<br>
//	 * redis> SPOP myset 3<br>
//	 * 1) "three"<br>
//	 * 2) "four"<br>
//	 * 3) "five"<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "one"<br>
//	 * redis><br>
//	 * 
//	 * @param key
//	 * @param count
//	 * @return
//	 */
//	Set<byte[]> spop(final byte[] key, final long count);
//
//	byte[] srandmember(final byte[] key);
//
//	/**
//	 * <h1>随机获取一个或多个元素</h1> <br>
//	 * 
//	 * When called with just the key argument, return a random element from the set
//	 * value stored at key.
//	 * 
//	 * If the provided count argument is positive, return an array of distinct
//	 * elements. The array's length is either count or the set's cardinality
//	 * (SCARD), whichever is lower.
//	 * 
//	 * If called with a negative count, the behavior changes and the command is
//	 * allowed to return the same element multiple times. In this case, the number
//	 * of returned elements is the absolute value of the specified count.
//	 * 
//	 * redis> SADD myset one two three<br>
//	 * (integer) 3<br>
//	 * redis> SRANDMEMBER myset<br>
//	 * "three"<br>
//	 * redis> SRANDMEMBER myset 2<br>
//	 * 1) "one"<br>
//	 * 2) "two"<br>
//	 * redis> SRANDMEMBER myset -5<br>
//	 * 1) "one"<br>
//	 * 2) "one"<br>
//	 * 3) "two"<br>
//	 * 4) "one"<br>
//	 * 5) "three"<br>
//	 * redis> <br>
//	 * 
//	 * @param key
//	 * @param count
//	 * @return
//	 */
//	List<byte[]> srandmember(final byte[] key, final int count);
//
//	/**
//	 * <h1>从set中移除1个或多个元素。当最后1个元素移除时删除set</h1> <br>
//	 * 
//	 * Remove the specified members from the set stored at key. Specified members
//	 * that are not a member of this set are ignored. If key does not exist, it is
//	 * treated as an empty set and this command returns 0.
//	 * 
//	 * An error is returned when the value stored at key is not a set.
//	 * 
//	 * redis> SADD myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "two"<br>
//	 * (integer) 1<br>
//	 * redis> SADD myset "three"<br>
//	 * (integer) 1<br>
//	 * redis> SREM myset "one"<br>
//	 * (integer) 1<br>
//	 * redis> SREM myset "four"<br>
//	 * (integer) 0<br>
//	 * redis> SMEMBERS myset<br>
//	 * 1) "three"<br>
//	 * 2) "two"<br>
//	 * redis> <br>
//	 * 
//	 * @param key
//	 * @param member
//	 * @return
//	 */
//	Long srem(final byte[] key, final byte[]... member);
//
//	ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor);
//
//	/**
//	 * <h1>迭代set的元素</h1> <br>
//	 * 
//	 * See SCAN for SSCAN documentation.
//	 * 
//	 * @param key
//	 * @param cursor
//	 * @param params
//	 * @return
//	 */
//	ScanResult<byte[]> sscan(final byte[] key, final byte[] cursor, final ScanParams params);
//
//	/**
//	 * <h1>返回多个set的并集</h1> <br>
//	 * 
//	 * Returns the members of the set resulting from the union of all the given
//	 * sets.<br>
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SUNION key1 key2<br>
//	 * 1) "a"<br>
//	 * 2) "c"<br>
//	 * 3) "e"<br>
//	 * 4) "b"<br>
//	 * 5) "d"<br>
//	 * redis> <br>
//	 * 
//	 * @param keys
//	 * @return
//	 */
//	Set<byte[]> sunion(final byte[]... keys);
//
//	/**
//	 * <h1>另存为多个set的并集</h1> <br>
//	 * This command is equal to SUNION, but instead of returning the resulting set,
//	 * it is stored in destination.
//	 * 
//	 * If destination already exists, it is overwritten.
//	 * 
//	 * redis> SADD key1 "a"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "b"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key1 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "c"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "d"<br>
//	 * (integer) 1<br>
//	 * redis> SADD key2 "e"<br>
//	 * (integer) 1<br>
//	 * redis> SUNIONSTORE key key1 key2<br>
//	 * (integer) 5<br>
//	 * redis> SMEMBERS key<br>
//	 * 1) "a"<br>
//	 * 2) "c"<br>
//	 * 3) "e"<br>
//	 * 4) "b"<br>
//	 * 5) "d"<br>
//	 * redis> <br>
//	 * 
//	 * @param dstkey
//	 * @param keys
//	 * @return
//	 */
//	Long sunionstore(final byte[] dstkey, final byte[]... keys);
//}
