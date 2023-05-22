package io.github.icodegarden.commons.redis;

import java.util.Collection;
import java.util.List;

import io.github.icodegarden.commons.lang.annotation.NotNull;
import io.github.icodegarden.commons.lang.annotation.Nullable;
import io.github.icodegarden.commons.redis.args.KeyValue;
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.ScanArgs;
import io.github.icodegarden.commons.redis.args.ScoredValue;
import io.github.icodegarden.commons.redis.args.ScoredValueScanCursor;
import io.github.icodegarden.commons.redis.args.SortedSetOption;
import io.github.icodegarden.commons.redis.args.ZAddArgs;
import io.github.icodegarden.commons.redis.args.ZAggregateArgs;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface SortedSetBinaryCommands {

	/**
	 * pop出1个元素
	 */
	@Nullable
	KeyValue<byte[], ScoredValue<byte[]>> bzmpop(long timeout, SortedSetOption option, byte[]... keys);

	/**
	 * <h1>从1个或多个set中移除并返回元素，携带score。阻塞直到有元素或超时，当没有元素时删除set</h1><br>
	 * 
	 * BZMPOP is the blocking variant of ZMPOP.
	 * 
	 * When any of the sorted sets contains elements, this command behaves exactly
	 * like ZMPOP. When used inside a MULTI/EXEC block, this command behaves exactly
	 * like ZMPOP. When all sorted sets are empty, Redis will block the connection
	 * until another client adds members to one of the keys or until the timeout (a
	 * double value specifying the maximum number of seconds to block) elapses. A
	 * timeout of zero can be used to block indefinitely.
	 * 
	 * See ZMPOP for more information.
	 * 
	 * 语法：BZMPOP timeout numkeys key [key ...] <MIN | MAX> [COUNT count]
	 * 
	 * 
	 * @param timeout 0表示无限
	 * @param option
	 * @param count
	 * @param keys
	 * @return
	 */
	@Nullable
	KeyValue<byte[], List<ScoredValue<byte[]>>> bzmpop(long timeout, SortedSetOption option, int count, byte[]... keys);

	/**
	 * <h1>从1个或多个set中移除并返回score最大的元素。阻塞直到有元素或超时，当没有元素时删除set</h1><br>
	 * 
	 * BZPOPMAX is the blocking variant of the sorted set ZPOPMAX primitive.
	 * 
	 * It is the blocking version because it blocks the connection when there are no
	 * members to pop from any of the given sorted sets. A member with the highest
	 * score is popped from first sorted set that is non-empty, with the given keys
	 * being checked in the order that they are given.
	 * 
	 * The timeout argument is interpreted as a double value specifying the maximum
	 * number of seconds to block. A timeout of zero can be used to block
	 * indefinitely.
	 * 
	 * See the BZPOPMIN documentation for the exact semantics, since BZPOPMAX is
	 * identical to BZPOPMIN with the only difference being that it pops members
	 * with the highest scores instead of popping the ones with the lowest scores.
	 * 
	 * redis> DEL zset1 zset2<br>
	 * (integer) 0<br>
	 * redis> ZADD zset1 0 a 1 b 2 c<br>
	 * (integer) 3<br>
	 * redis> BZPOPMAX zset1 zset2 0<br>
	 * 1) "zset1"<br>
	 * 2) "c"<br>
	 * 3) "2"<br>
	 * 
	 * @param timeout
	 * @param keys
	 * @return
	 */
	@Nullable
	KeyValue<byte[], ScoredValue<byte[]>> bzpopmax(double timeout, byte[]... keys);

	/**
	 * <h1>从1个或多个set中移除并返回score最小的元素。阻塞直到有元素或超时，当没有元素时删除set</h1><br>
	 * 
	 * BZPOPMIN is the blocking variant of the sorted set ZPOPMIN primitive.
	 * 
	 * It is the blocking version because it blocks the connection when there are no
	 * members to pop from any of the given sorted sets. A member with the lowest
	 * score is popped from first sorted set that is non-empty, with the given keys
	 * being checked in the order that they are given.
	 * 
	 * The timeout argument is interpreted as a double value specifying the maximum
	 * number of seconds to block. A timeout of zero can be used to block
	 * indefinitely.
	 * 
	 * See the BLPOP documentation for the exact semantics, since BZPOPMIN is
	 * identical to BLPOP with the only difference being the data structure being
	 * popped from.
	 * 
	 * redis> DEL zset1 zset2<br>
	 * (integer) 0<br>
	 * redis> ZADD zset1 0 a 1 b 2 c<br>
	 * (integer) 3<br>
	 * redis> BZPOPMIN zset1 zset2 0<br>
	 * 1) "zset1"<br>
	 * 2) "a"<br>
	 * 3) "0"<br>
	 * 
	 * @param timeout
	 * @param keys
	 * @return
	 */
	@Nullable
	KeyValue<byte[], ScoredValue<byte[]>> bzpopmin(double timeout, byte[]... keys);

	long zadd(byte[] key, double score, byte[] member);

	long zadd(byte[] key, double score, byte[] member, ZAddArgs params);

	long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues);

	/**
	 * <h1>向set中新增1个或多个元素，或更新元素的score</h1><br>
	 * 
	 * Adds all the specified members with the specified scores to the sorted set
	 * stored at key. It is possible to specify multiple score / member pairs. If a
	 * specified member is already a member of the sorted set, the score is updated
	 * and the element reinserted at the right position to ensure the correct
	 * ordering.
	 * 
	 * If key does not exist, a new sorted set with the specified members as sole
	 * members is created, like if the sorted set was empty. If the key exists but
	 * does not hold a sorted set, an error is returned.
	 * 
	 * The score values should be the string representation of a double precision
	 * floating point number. +inf and -inf values are valid values as well.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 1 "uno"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two" 3 "three"<br>
	 * (integer) 2<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * 1) "one"<br>
	 * 2) "1"<br>
	 * 3) "uno"<br>
	 * 4) "1"<br>
	 * 5) "two"<br>
	 * 6) "2"<br>
	 * 7) "three"<br>
	 * 8) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * 
	 * @param key
	 * @param scoreMembers
	 * @param params       XX: Only update elements that already exist. Don't add
	 *                     new elements.<br>
	 *                     NX: Only add new elements. Don't update already existing
	 *                     elements.<br>
	 *                     LT: Only update existing elements if the new score is
	 *                     less than the current score. This flag doesn't prevent
	 *                     adding new elements.<br>
	 *                     GT: Only update existing elements if the new score is
	 *                     greater than the current score. This flag doesn't prevent
	 *                     adding new elements.<br>
	 *                     CH: Modify the return value from the number of new
	 *                     elements added, to the total number of elements changed
	 *                     (CH is an abbreviation of changed). Changed elements are
	 *                     new elements added and elements already existing for
	 *                     which the score was updated. So elements specified in the
	 *                     command line having the same score as they had in the
	 *                     past are not counted. Note: normally the return value of
	 *                     ZADD only counts the number of new elements added.<br>
	 *                     INCR: When this option is specified ZADD acts like
	 *                     ZINCRBY. Only one score-element pair can be specified in
	 *                     this mode.<br>
	 *                     Note: The GT, LT and NX options are mutually
	 *                     exclusive.<br>
	 * @return
	 */
	long zadd(byte[] key, Collection<ScoredValue<byte[]>> scoredValues, ZAddArgs params);

	/**
	 * <h1>获取元素总数</h1><br>
	 * 
	 * Returns the sorted set cardinality (number of elements) of the sorted set
	 * stored at key.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZCARD myzset<br>
	 * (integer) 2<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @return Integer reply: the cardinality (number of elements) of the sorted
	 *         set, or 0 if key does not exist.
	 */
	long zcard(byte[] key);

	/**
	 * <h1>根据score的范围，获取元素总数</h1><br>
	 * ZCOUNT key min max<br>
	 * 
	 * Returns the number of elements in the sorted set at key with a score between
	 * min and max.
	 * 
	 * The min and max arguments have the same semantic as described for
	 * ZRANGEBYSCORE.
	 * 
	 * Note: the command has a complexity of just O(log(N)) because it uses elements
	 * ranks (see ZRANK) to get an idea of the range. Because of this there is no
	 * need to do a work proportional to the size of the range.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZCOUNT myzset -inf +inf<br>
	 * (integer) 3<br>
	 * redis> ZCOUNT myzset (1 3<br>
	 * (integer) 2<br>
	 * redis> <br>
	 * 
	 * 
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	long zcount(byte[] key, Range<? extends Number> range);

	@NotNull
	List<byte[]> zdiff(byte[]... keys);

	/**
	 * <h1>返回多个set之间的差集</h1><br>
	 * 
	 * This command is similar to ZDIFFSTORE, but instead of storing the resulting
	 * sorted set, it is returned to the client.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZDIFF 2 zset1 zset2<br>
	 * 1) "three"<br>
	 * redis> ZDIFF 2 zset1 zset2 WITHSCORES<br>
	 * 1) "three"<br>
	 * 2) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param keys
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zdiffWithScores(byte[]... keys);

	/**
	 * <h1>另存多个set之间的差集</h1><br>
	 * 
	 * Computes the difference between the first and all successive input sorted
	 * sets and stores the result in destination. The total number of input keys is
	 * specified by numkeys.
	 * 
	 * Keys that do not exist are considered to be empty sets.
	 * 
	 * If destination already exists, it is overwritten.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZDIFFSTORE out 2 zset1 zset2<br>
	 * (integer) 1<br>
	 * redis> ZRANGE out 0 -1 WITHSCORES<br>
	 * 1) "three"<br>
	 * 2) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param dstkey
	 * @param keys
	 * @return
	 */
	long zdiffStore(byte[] dstkey, byte[]... keys);

	/**
	 * <h1>原子操作元素的score</h1><br>
	 * 
	 * Increments the score of member in the sorted set stored at key by increment.
	 * If member does not exist in the sorted set, it is added with increment as its
	 * score (as if its previous score was 0.0). If key does not exist, a new sorted
	 * set with the specified member as its sole member is created.
	 * 
	 * An error is returned when key exists but does not hold a sorted set.
	 * 
	 * The score value should be the string representation of a numeric value, and
	 * accepts double precision floating point numbers. It is possible to provide a
	 * negative value to decrement the score.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZINCRBY myzset 2 "one"<br>
	 * "3"<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * 1) "two"<br>
	 * 2) "2"<br>
	 * 3) "one"<br>
	 * 4) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param increment
	 * @param member
	 * @param params
	 * @return
	 */
	double zincrby(byte[] key, double increment, byte[] member);

	@NotNull
	List<byte[]> zinter(byte[]... keys);

	@NotNull
	List<byte[]> zinter(ZAggregateArgs params, byte[]... keys);

	@NotNull
	List<ScoredValue<byte[]>> zinterWithScores(byte[]... keys);

	/**
	 * <h1>返回多个set之间的交集</h1><br>
	 * 
	 * This command is similar to ZINTERSTORE, but instead of storing the resulting
	 * sorted set, it is returned to the client.
	 * 
	 * For a description of the WEIGHTS and AGGREGATE options, see ZUNIONSTORE.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZINTER 2 zset1 zset2<br>
	 * 1) "one"<br>
	 * 2) "two"<br>
	 * redis> ZINTER 2 zset1 zset2 WITHSCORES<br>
	 * 1) "one"<br>
	 * 2) "2"<br>
	 * 3) "two"<br>
	 * 4) "4"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param params
	 * @param keys
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zinterWithScores(ZAggregateArgs params, byte[]... keys);

	long zinterstore(byte[] dstkey, byte[]... sets);

	/**
	 * <h1>另存多个set之间的交集</h1><br>
	 * 
	 * Computes the intersection of numkeys sorted sets given by the specified keys,
	 * and stores the result in destination. It is mandatory to provide the number
	 * of input keys (numkeys) before passing the input keys and the other
	 * (optional) arguments.
	 * 
	 * By default, the resulting score of an element is the sum of its scores in the
	 * sorted sets where it exists. Because intersection requires an element to be a
	 * member of every given sorted set, this results in the score of every element
	 * in the resulting sorted set to be equal to the number of input sorted sets.
	 * 
	 * For a description of the WEIGHTS and AGGREGATE options, see ZUNIONSTORE.
	 * 
	 * If destination already exists, it is overwritten.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZINTERSTORE out 2 zset1 zset2 WEIGHTS 2 3<br>
	 * (integer) 2<br>
	 * redis> ZRANGE out 0 -1 WITHSCORES<br>
	 * 1) "one"<br>
	 * 2) "5"<br>
	 * 3) "two"<br>
	 * 4) "10"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 */
	long zinterstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets);

	long zintercard(byte[]... keys);

	/**
	 * <h1>返回多个set之间的交集的元素个数</h1><br>
	 * 
	 * This command is similar to ZINTER, but instead of returning the result set,
	 * it returns just the cardinality of the result.
	 * 
	 * Keys that do not exist are considered to be empty sets. With one of the keys
	 * being an empty set, the resulting set is also empty (since set intersection
	 * with an empty set always results in an empty set).
	 * 
	 * By default, the command calculates the cardinality of the intersection of all
	 * given sets. When provided with the optional LIMIT argument (which defaults to
	 * 0 and means unlimited), if the intersection cardinality reaches limit partway
	 * through the computation, the algorithm will exit and yield limit as the
	 * cardinality. Such implementation ensures a significant speedup for queries
	 * where the limit is lower than the actual intersection cardinality.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZINTER 2 zset1 zset2<br>
	 * 1) "one"<br>
	 * 2) "two"<br>
	 * redis> ZINTERCARD 2 zset1 zset2<br>
	 * (integer) 2<br>
	 * redis> ZINTERCARD 2 zset1 zset2 LIMIT 1<br>
	 * (integer) 1<br>
	 * redis> <br>
	 * 
	 * 
	 * @param limit 最大统计值，0表示不限制
	 * @param keys
	 * @return
	 */
	long zintercard(long limit, byte[]... keys);

	/**
	 * <h1>按字典排序返回值是min和max之间的元素个数，可以用于score相同的场景</h1><br>
	 * 
	 * When all the elements in a sorted set are inserted with the same score, in
	 * order to force lexicographical ordering, this command returns the number of
	 * elements in the sorted set at key with a value between min and max.
	 * 
	 * The min and max arguments have the same meaning as described for ZRANGEBYLEX.
	 * 
	 * Note: the command has a complexity of just O(log(N)) because it uses elements
	 * ranks (see ZRANK) to get an idea of the range. Because of this there is no
	 * need to do a work proportional to the size of the range.
	 * 
	 * redis> ZADD myzset 0 a 0 b 0 c 0 d 0 e<br>
	 * (integer) 5<br>
	 * redis> ZADD myzset 0 f 0 g<br>
	 * (integer) 2<br>
	 * redis> ZLEXCOUNT myzset - +<br>
	 * (integer) 7<br>
	 * redis> ZLEXCOUNT myzset [b [f<br>
	 * (integer) 5<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	long zlexcount(byte[] key, byte[] min, byte[] max);

	/**
	 * pop出1个
	 */
	@Nullable
	KeyValue<byte[], ScoredValue<byte[]>> zmpop(SortedSetOption option, byte[]... keys);

	/**
	 * <h1>从1个或多个set中pop score最大或最小的元素。多个set时每次只会选中1个set</h1><br>
	 * 
	 * Pops one or more elements, that are member-score pairs, from the first
	 * non-empty sorted set in the provided list of key names.
	 * 
	 * ZMPOP and BZMPOP are similar to the following, more limited, commands:
	 * 
	 * ZPOPMIN or ZPOPMAX which take only one key, and can return multiple elements.
	 * BZPOPMIN or BZPOPMAX which take multiple keys, but return only one element
	 * from just one key. See BZMPOP for the blocking variant of this command.
	 * 
	 * When the MIN modifier is used, the elements popped are those with the lowest
	 * scores from the first non-empty sorted set. The MAX modifier causes elements
	 * with the highest scores to be popped. The optional COUNT can be used to
	 * specify the number of elements to pop, and is set to 1 by default.
	 * 
	 * The number of popped elements is the minimum from the sorted set's
	 * cardinality and COUNT's value.
	 * 
	 * redis> ZMPOP 1 notsuchkey MIN<br>
	 * (error) object of type 'NoneType' has no len()<br>
	 * redis> ZADD myzset 1 "one" 2 "two" 3 "three"<br>
	 * (integer) 3<br>
	 * redis> ZMPOP 1 myzset MIN<br>
	 * 1) "myzset"<br>
	 * 2) 1) 1) "one"<br>
	 * 2) "1"<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * 1) "two"<br>
	 * 2) "2"<br>
	 * 3) "three"<br>
	 * 4) "3"<br>
	 * redis> ZMPOP 1 myzset MAX COUNT 10<br>
	 * 1) "myzset"<br>
	 * 2) 1) 1) "three"<br>
	 * 2) "3"<br>
	 * 2) 1) "two"<br>
	 * 2) "2"<br>
	 * redis> ZADD myzset2 4 "four" 5 "five" 6 "six"<br>
	 * (integer) 3<br>
	 * redis> ZMPOP 2 myzset myzset2 MIN COUNT 10<br>
	 * 1) "myzset2"<br>
	 * 2) 1) 1) "four"<br>
	 * 2) "4"<br>
	 * 2) 1) "five"<br>
	 * 2) "5"<br>
	 * 3) 1) "six"<br>
	 * 2) "6"<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * (empty array)<br>
	 * redis> ZMPOP 2 myzset myzset2 MAX COUNT 10<br>
	 * (error) object of type 'NoneType' has no len()<br>
	 * redis> ZRANGE myzset2 0 -1 WITHSCORES<br>
	 * (empty array)<br>
	 * redis> EXISTS myzset myzset2<br>
	 * (integer) 0<br>
	 * redis> <br>
	 * 
	 * 
	 * @param option
	 * @param count  pop的数量，默认1
	 * @param keys
	 * @return
	 */
	@Nullable
	KeyValue<byte[], List<ScoredValue<byte[]>>> zmpop(SortedSetOption option, int count, byte[]... keys);

	/**
	 * <h1>获取指定元素的score</h1><br>
	 * 
	 * Returns the scores associated with the specified members in the sorted set
	 * stored at key.
	 * 
	 * For every member that does not exist in the sorted set, a nil value is
	 * returned.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZMSCORE myzset "one" "two" "nofield"<br>
	 * 1) "1"<br>
	 * 2) "2"<br>
	 * 3) (nil)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param members
	 * @return
	 */
	@NotNull
	List<Double> zmscore(byte[] key, byte[]... members);

	@NotNull
	ScoredValue<byte[]> zpopmax(byte[] key);

	/**
	 * <h1>pop并返回score最大的元素</h1><br>
	 * 
	 * Removes and returns up to count members with the highest scores in the sorted
	 * set stored at key.
	 * 
	 * When left unspecified, the default value for count is 1. Specifying a count
	 * value that is higher than the sorted set's cardinality will not produce an
	 * error. When returning multiple elements, the one with the highest score will
	 * be the first, followed by the elements with lower scores.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZPOPMAX myzset<br>
	 * 1) "three"<br>
	 * 2) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param count pop的数量，默认1
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zpopmax(byte[] key, int count);

	@NotNull
	ScoredValue<byte[]> zpopmin(byte[] key);

	/**
	 * <h1>pop并返回score最小的元素。同zpopmax</h1><br>
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZPOPMIN myzset<br>
	 * 1) "one"<br>
	 * 2) "1"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param count pop的数量，默认1
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zpopmin(byte[] key, int count);

	@Nullable
	byte[] zrandmember(byte[] key);

	@NotNull
	List<byte[]> zrandmember(byte[] key, long count);

	/**
	 * <h1>随机返回1个或多个元素</h1><br>
	 * 
	 * When called with just the key argument, return a random element from the
	 * sorted set value stored at key.
	 * 
	 * If the provided count argument is positive, return an array of distinct
	 * elements. The array's length is either count or the sorted set's cardinality
	 * (ZCARD), whichever is lower.
	 * 
	 * If called with a negative count, the behavior changes and the command is
	 * allowed to return the same element multiple times. In this case, the number
	 * of returned elements is the absolute value of the specified count.
	 * 
	 * The optional WITHSCORES modifier changes the reply so it includes the
	 * respective scores of the randomly selected elements from the sorted set.
	 * 
	 * redis> ZADD dadi 1 uno 2 due 3 tre 4 quattro 5 cinque 6 sei<br>
	 * (integer) 6<br>
	 * redis> ZRANDMEMBER dadi<br>
	 * "due"<br>
	 * redis> ZRANDMEMBER dadi<br>
	 * "cinque"<br>
	 * redis> ZRANDMEMBER dadi -5 WITHSCORES<br>
	 * 1) "cinque"<br>
	 * 2) "5"<br>
	 * 3) "due"<br>
	 * 4) "2"<br>
	 * 5) "cinque"<br>
	 * 6) "5"<br>
	 * 7) "uno"<br>
	 * 8) "1"<br>
	 * 9) "due"<br>
	 * 10) "2"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param count
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zrandmemberWithScores(byte[] key, long count);

	@NotNull
	List<byte[]> zrange(byte[] key, long start, long stop);

	/**
	 * <h1>获取range范围内的元素</h1><br>
	 * 
	 * Returns the specified range of elements in the sorted set stored at <key>.
	 * 
	 * ZRANGE can perform different types of range queries: by index (rank), by the
	 * score, or by lexicographical order.
	 * 
	 * Starting with Redis 6.2.0, this command can replace the following commands:
	 * ZREVRANGE, ZRANGEBYSCORE, ZREVRANGEBYSCORE, ZRANGEBYLEX and ZREVRANGEBYLEX.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZRANGE myzset 0 -1<br>
	 * 1) "one"<br>
	 * 2) "two"<br>
	 * 3) "three"<br>
	 * redis> ZRANGE myzset 2 3<br>
	 * 1) "three"<br>
	 * redis> ZRANGE myzset -2 -1<br>
	 * 1) "two"<br>
	 * 2) "three"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param zRangeParams
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zrangeWithScores(byte[] key, long start, long stop);

	@NotNull
	List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range);

	/**
	 * <h1>按字典排序返回值是min和max之间的元素，可以用于score相同的场景</h1><br>
	 * 
	 * When all the elements in a sorted set are inserted with the same score, in
	 * order to force lexicographical ordering, this command returns all the
	 * elements in the sorted set at key with a value between min and max.
	 * 
	 * If the elements in the sorted set have different scores, the returned
	 * elements are unspecified.
	 * 
	 * The elements are considered to be ordered from lower to higher strings as
	 * compared byte-by-byte using the memcmp() C function. Longer strings are
	 * considered greater than shorter strings if the common part is identical.
	 * 
	 * The optional LIMIT argument can be used to only get a range of the matching
	 * elements (similar to SELECT LIMIT offset, count in SQL). A negative count
	 * returns all elements from the offset. Keep in mind that if offset is large,
	 * the sorted set needs to be traversed for offset elements before getting to
	 * the elements to return, which can add up to O(N) time complexity.
	 * 
	 * redis> ZADD myzset 0 a 0 b 0 c 0 d 0 e 0 f 0 g<br>
	 * (integer) 7<br>
	 * redis> ZRANGEBYLEX myzset - [c<br>
	 * 1) "a"<br>
	 * 2) "b"<br>
	 * 3) "c"<br>
	 * redis> ZRANGEBYLEX myzset - (c<br>
	 * 1) "a"<br>
	 * 2) "b"<br>
	 * redis> ZRANGEBYLEX myzset [aaa (g<br>
	 * 1) "b"<br>
	 * 2) "c"<br>
	 * 3) "d"<br>
	 * 4) "e"<br>
	 * 5) "f"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @param offset 结果集中跳过多少
	 * @param count  限制数量，默认不限制
	 * @return
	 */
	@NotNull
	List<byte[]> zrangeByLex(byte[] key, Range<byte[]> range, int offset, int count);

	@NotNull
	List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range);

	@NotNull
	List<byte[]> zrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count);

	@NotNull
	List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range);

	/**
	 * <h1>获取range范围内的元素</h1><br>
	 * 
	 * Returns all the elements in the sorted set at key with a score between min
	 * and max (including elements with score equal to min or max). The elements are
	 * considered to be ordered from low to high scores.
	 * 
	 * The elements having the same score are returned in lexicographical order
	 * (this follows from a property of the sorted set implementation in Redis and
	 * does not involve further computation).
	 * 
	 * The optional LIMIT argument can be used to only get a range of the matching
	 * elements (similar to SELECT LIMIT offset, count in SQL). A negative count
	 * returns all elements from the offset. Keep in mind that if offset is large,
	 * the sorted set needs to be traversed for offset elements before getting to
	 * the elements to return, which can add up to O(N) time complexity.
	 * 
	 * The optional WITHSCORES argument makes the command return both the element
	 * and its score, instead of the element alone. This option is available since
	 * Redis 2.0.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZRANGEBYSCORE myzset -inf +inf<br>
	 * 1) "one"<br>
	 * 2) "two"<br>
	 * 3) "three"<br>
	 * redis> ZRANGEBYSCORE myzset 1 2<br>
	 * 1) "one"<br>
	 * 2) "two"<br>
	 * redis> ZRANGEBYSCORE myzset (1 2<br>
	 * 1) "two"<br>
	 * redis> ZRANGEBYSCORE myzset (1 (2<br>
	 * (empty array)<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @param offset 结果集中跳过多少
	 * @param count  限制数量，默认不限制
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset, int count);

	/**
	 * <h1>另存range范围内的元素</h1><br>
	 * ZRANGESTORE dst src min max [BYSCORE | BYLEX] [REV] [LIMIT offset count]<br>
	 * 
	 * This command is like ZRANGE, but stores the result in the <dst> destination
	 * key.
	 * 
	 * redis> ZADD srczset 1 "one" 2 "two" 3 "three" 4 "four"<br>
	 * (integer) 4<br>
	 * redis> ZRANGESTORE dstzset srczset 2 -1<br>
	 * (integer) 2<br>
	 * redis> ZRANGE dstzset 0 -1<br>
	 * 1) "three"<br>
	 * 2) "four"<br>
	 * redis> <br>
	 * 
	 * @param dest
	 * @param src
	 * @param range 表示index的start和stop
	 * @return
	 */
	long zrangestore(byte[] dest, byte[] src, Range<Long> range);

	long zrangestoreByLex(byte[] dest, byte[] src, Range<byte[]> range, int offset, int count);

	long zrangestoreByScore(byte[] dest, byte[] src, Range<? extends Number> range, int offset, int count);

	/**
	 * <h1>返回元素的score排序是第几个，排序从小到大</h1><br>
	 * 
	 * Returns the rank of member in the sorted set stored at key, with the scores
	 * ordered from low to high. The rank (or index) is 0-based, which means that
	 * the member with the lowest score has rank 0.
	 * 
	 * The optional WITHSCORE argument supplements the command's reply with the
	 * score of the element returned.
	 * 
	 * Use ZREVRANK to get the rank of an element with the scores ordered from high
	 * to low.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZRANK myzset "three"<br>
	 * (integer) 2<br>
	 * redis> ZRANK myzset "four"<br>
	 * (nil)<br>
	 * redis> ZRANK myzset "three" WITHSCORE<br>
	 * (error) ERR wrong number of arguments for command<br>
	 * redis> ZRANK myzset "four" WITHSCORE<br>
	 * (error) ERR wrong number of arguments for command<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param member
	 * @return 首位是0
	 */
	@Nullable
	Long zrank(byte[] key, byte[] member);

	/**
	 * <h1>移除1个或多个元素</h1><br>
	 * 
	 * Removes the specified members from the sorted set stored at key. Non existing
	 * members are ignored.
	 * 
	 * An error is returned when key exists and does not hold a sorted set.
	 * 
	 * redis> ZADD myzset 1 "one" (integer) 1 <br>
	 * redis> ZADD myzset 2 "two" (integer) 1 <br>
	 * redis> ZADD myzset 3 "three" (integer) 1 <br>
	 * redis> ZREM myzset "two" (integer) 1 <br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES 1) "one" 2) "1" 3) "three" 4) "3" <br>
	 * redis><br>
	 * 
	 * @param key
	 * @param members
	 * @return 移除的个数
	 */
	long zrem(byte[] key, byte[]... members);

	/**
	 * <h1>根据rangeByLex移除1个或多个元素</h1><br>
	 * 
	 * When all the elements in a sorted set are inserted with the same score, in
	 * order to force lexicographical ordering, this command removes all elements in
	 * the sorted set stored at key between the lexicographical range specified by
	 * min and max.
	 * 
	 * The meaning of min and max are the same of the ZRANGEBYLEX command.
	 * Similarly, this command actually removes the same elements that ZRANGEBYLEX
	 * would return if called with the same min and max arguments.
	 * 
	 * redis> ZADD myzset 0 aaaa 0 b 0 c 0 d 0 e<br>
	 * (integer) 5<br>
	 * redis> ZADD myzset 0 foo 0 zap 0 zip 0 ALPHA 0 alpha<br>
	 * (integer) 5<br>
	 * redis> ZRANGE myzset 0 -1<br>
	 * 1) "ALPHA"<br>
	 * 2) "aaaa"<br>
	 * 3) "alpha"<br>
	 * 4) "b"<br>
	 * 5) "c"<br>
	 * 6) "d"<br>
	 * 7) "e"<br>
	 * 8) "foo"<br>
	 * 9) "zap"<br>
	 * 10) "zip"<br>
	 * redis> ZREMRANGEBYLEX myzset [alpha [omega<br>
	 * (integer) 6<br>
	 * redis> ZRANGE myzset 0 -1<br>
	 * 1) "ALPHA"<br>
	 * 2) "aaaa"<br>
	 * 3) "zap"<br>
	 * 4) "zip"<br>
	 * redis> <br>
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	long zremrangeByLex(byte[] key, Range<byte[]> range);

	/**
	 * <h1>根据rangeByRank移除1个或多个元素</h1><br>
	 * 
	 * Removes all elements in the sorted set stored at key with rank between start
	 * and stop. Both start and stop are 0 -based indexes with 0 being the element
	 * with the lowest score. These indexes can be negative numbers, where they
	 * indicate offsets starting at the element with the highest score. For example:
	 * -1 is the element with the highest score, -2 the element with the second
	 * highest score and so forth.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZREMRANGEBYRANK myzset 0 1<br>
	 * (integer) 2<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * 1) "three"<br>
	 * 2) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param start
	 * @param stop
	 * @return
	 */
	long zremrangeByRank(byte[] key, long start, long stop);

	/**
	 * <h1>根据rangeByScore移除1个或多个元素</h1><br>
	 * 
	 * Removes all elements in the sorted set stored at key with a score between min
	 * and max (inclusive).
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD myzset 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZREMRANGEBYSCORE myzset -inf (2<br>
	 * (integer) 1<br>
	 * redis> ZRANGE myzset 0 -1 WITHSCORES<br>
	 * 1) "two"<br>
	 * 2) "2"<br>
	 * 3) "three"<br>
	 * 4) "3"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param min
	 * @param max
	 * @return
	 */
	long zremrangeByScore(byte[] key, Range<? extends Number> range);

	@NotNull
	List<byte[]> zrevrange(byte[] key, long start, long stop);

	/**
	 * <h1>zrange的倒排</h1><br>
	 * 
	 * @param key
	 * @param start
	 * @param stop
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zrevrangeWithScores(byte[] key, long start, long stop);

	@NotNull
	List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range);

	/**
	 * <h1>zrangeByLex的倒排</h1><br>
	 * 
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 */
	@NotNull
	List<byte[]> zrevrangeByLex(byte[] key, Range<byte[]> range, int offset, int count);

	@NotNull
	List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range);

	@NotNull
	List<byte[]> zrevrangeByScore(byte[] key, Range<? extends Number> range, int offset, int count);

	@NotNull
	List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range);

	/**
	 * <h1>zrangeByScore的倒排</h1><br>
	 * 
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zrevrangeByScoreWithScores(byte[] key, Range<? extends Number> range, int offset,
			int count);

	/**
	 * <h1>zrank的倒排</h1><br>
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	@Nullable
	Long zrevrank(byte[] key, byte[] member);

	@NotNull
	ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor);

	/**
	 * <h1>迭代zset</h1><br>
	 * 
	 * @param key
	 * @param cursor
	 * @param params
	 * @return
	 */
	@NotNull
	ScoredValueScanCursor<byte[]> zscan(byte[] key, byte[] cursor, ScanArgs params);

	/**
	 * <h1>返回元素的score</h1><br>
	 * 
	 * Returns the score of member in the sorted set at key.
	 * 
	 * If member does not exist in the sorted set, or key does not exist, nil is
	 * returned.
	 * 
	 * redis> ZADD myzset 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZSCORE myzset "one"<br>
	 * "1"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param key
	 * @param member
	 * @return
	 */
	@Nullable
	Double zscore(byte[] key, byte[] member);

	@NotNull
	List<byte[]> zunion(byte[]... keys);

	@NotNull
	List<byte[]> zunion(ZAggregateArgs params, byte[]... keys);

	@NotNull
	List<ScoredValue<byte[]>> zunionWithScores(byte[]... keys);

	/**
	 * <h1>返回多个zset之间的并集</h1><br>
	 * 
	 * This command is similar to ZUNIONSTORE, but instead of storing the resulting
	 * sorted set, it is returned to the client.
	 * 
	 * For a description of the WEIGHTS and AGGREGATE options, see ZUNIONSTORE.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZUNION 2 zset1 zset2<br>
	 * 1) "one"<br>
	 * 2) "three"<br>
	 * 3) "two"<br>
	 * redis> ZUNION 2 zset1 zset2 WITHSCORES<br>
	 * 1) "one"<br>
	 * 2) "2"<br>
	 * 3) "three"<br>
	 * 4) "3"<br>
	 * 5) "two"<br>
	 * 6) "4"<br>
	 * redis> <br>
	 * 
	 * @param params
	 * @param keys
	 * @return
	 */
	@NotNull
	List<ScoredValue<byte[]>> zunionWithScores(ZAggregateArgs params, byte[]... keys);

	long zunionstore(byte[] dstkey, byte[]... sets);

	/**
	 * <h1>另存多个zset之间的并集</h1><br>
	 * 
	 * Computes the union of numkeys sorted sets given by the specified keys, and
	 * stores the result in destination. It is mandatory to provide the number of
	 * input keys (numkeys) before passing the input keys and the other (optional)
	 * arguments.
	 * 
	 * By default, the resulting score of an element is the sum of its scores in the
	 * sorted sets where it exists.
	 * 
	 * Using the WEIGHTS option, it is possible to specify a multiplication factor
	 * for each input sorted set. This means that the score of every element in
	 * every input sorted set is multiplied by this factor before being passed to
	 * the aggregation function. When WEIGHTS is not given, the multiplication
	 * factors default to 1.
	 * 
	 * With the AGGREGATE option, it is possible to specify how the results of the
	 * union are aggregated. This option defaults to SUM, where the score of an
	 * element is summed across the inputs where it exists. When this option is set
	 * to either MIN or MAX, the resulting set will contain the minimum or maximum
	 * score of an element across the inputs where it exists.
	 * 
	 * If destination already exists, it is overwritten.
	 * 
	 * redis> ZADD zset1 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset1 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 1 "one"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 2 "two"<br>
	 * (integer) 1<br>
	 * redis> ZADD zset2 3 "three"<br>
	 * (integer) 1<br>
	 * redis> ZUNIONSTORE out 2 zset1 zset2 WEIGHTS 2 3<br>
	 * (integer) 3<br>
	 * redis> ZRANGE out 0 -1 WITHSCORES<br>
	 * 1) "one"<br>
	 * 2) "5"<br>
	 * 3) "three"<br>
	 * 4) "9"<br>
	 * 5) "two"<br>
	 * 6) "10"<br>
	 * redis> <br>
	 * 
	 * 
	 * @param dstkey
	 * @param params
	 * @param sets
	 * @return
	 */
	long zunionstore(byte[] dstkey, ZAggregateArgs params, byte[]... sets);
}
