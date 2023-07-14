package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Map;

import io.github.icodegarden.commons.redis.args.ClaimedMessages;
import io.github.icodegarden.commons.redis.args.Limit;
import io.github.icodegarden.commons.redis.args.PendingMessage;
import io.github.icodegarden.commons.redis.args.PendingMessages;
import io.github.icodegarden.commons.redis.args.Range;
import io.github.icodegarden.commons.redis.args.StreamMessage;
import io.github.icodegarden.commons.redis.args.XAddArgs;
import io.github.icodegarden.commons.redis.args.XAutoClaimArgs;
import io.github.icodegarden.commons.redis.args.XClaimArgs;
import io.github.icodegarden.commons.redis.args.XGroupCreateArgs;
import io.github.icodegarden.commons.redis.args.XPendingArgs;
import io.github.icodegarden.commons.redis.args.XReadArgs;
import io.github.icodegarden.commons.redis.args.XTrimArgs;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface StreamBinaryCommands {

	/**
	 * <h1>ack被消费者group消费成功的消息ids</h1><br>
	 * 
	 * XACK key group id [id ...]
	 * 
	 * The XACK command removes one or multiple messages from the Pending Entries
	 * List (PEL) of a stream consumer group. A message is pending, and as such
	 * stored inside the PEL, when it was delivered to some consumer, normally as a
	 * side effect of calling XREADGROUP, or when a consumer took ownership of a
	 * message calling XCLAIM. The pending message was delivered to some consumer
	 * but the server is yet not sure it was processed at least once. So new calls
	 * to XREADGROUP to grab the messages history for a consumer (for instance using
	 * an ID of 0), will return such message. Similarly the pending message will be
	 * listed by the XPENDING command, that inspects the PEL.
	 * 
	 * Once a consumer successfully processes a message, it should call XACK so that
	 * such message does not get processed again, and as a side effect, the PEL
	 * entry about this message is also purged, releasing memory from the Redis
	 * server.
	 * 
	 * redis> XACK mystream mygroup 1526569495631-0<br>
	 * (integer) 1<br>
	 * 
	 * @return The command returns the number of messages successfully acknowledged.
	 *         Certain message IDs may no longer be part of the PEL (for example
	 *         because they have already been acknowledged), and XACK will not count
	 *         them as successfully acknowledged.
	 */
	long xack(byte[] key, byte[] group, String... ids);

	String xadd(byte[] key, Map<byte[], byte[]> hash);

	/**
	 * <h1>发送消息到stream，key不存在则创建</h1><br>
	 * 
	 * XADD key [NOMKSTREAM] [<MAXLEN | MINID> [= | ~] threshold<br>
	 * [LIMIT count]] <* | id> field value [field value ...]
	 * 
	 * Appends the specified stream entry to the stream at the specified key. If the
	 * key does not exist, as a side effect of running this command the key is
	 * created with a stream value. The creation of stream's key can be disabled
	 * with the NOMKSTREAM option.
	 * 
	 * An entry is composed of a list of field-value pairs. The field-value pairs
	 * are stored in the same order they are given by the user. Commands that read
	 * the stream, such as XRANGE or XREAD, are guaranteed to return the fields and
	 * values exactly in the same order they were added by XADD.
	 * 
	 * XADD is the only Redis command that can add data to a stream, but there are
	 * other commands, such as XDEL and XTRIM, that are able to remove data from a
	 * stream. server.
	 * 
	 * redis> XADD mystream * name Sara surname OConnor<br>
	 * "1689229261932-0"<br>
	 * redis> XADD mystream * field1 value1 field2 value2 field3 value3<br>
	 * "1689229261933-0"<br>
	 * redis> XLEN mystream<br>
	 * (integer) 2<br>
	 * redis> XRANGE mystream - +<br>
	 * 1) 1) "1689229261932-0"<br>
	 * 2) 1) "name"<br>
	 * 2) "Sara"<br>
	 * 3) "surname"<br>
	 * 4) "OConnor"<br>
	 * 2) 1) "1689229261933-0"<br>
	 * 
	 * @return The command returns the ID of the added entry. The ID is the one
	 *         auto-generated if * is passed as ID argument, otherwise the command
	 *         just returns the same ID specified by the user during insertion.
	 * 
	 *         The command returns a Null reply when used with the NOMKSTREAM option
	 *         and the key doesn't exist.
	 */
	String xadd(byte[] key, XAddArgs args, Map<byte[], byte[]> hash);

	/**
	 * <h1>Changes, or acquires, ownership of messages in a consumer group, as if
	 * the messages were delivered to as consumer group member.</h1><br>
	 * 
	 * XAUTOCLAIM key group consumer min-idle-time start [COUNT count] [JUSTID]
	 * 
	 * 
	 * > XAUTOCLAIM mystream mygroup Alice 3600000 0-0 COUNT 25<br>
	 * 1) "0-0"<br>
	 * 2) 1) 1) "1609338752495-0"<br>
	 * 2) 1) "field"<br>
	 * 2) "value"<br>
	 * 3) (empty array)<br>
	 * 
	 * @return An array with three elements:
	 * 
	 *         A stream ID to be used as the <start> argument for the next call to
	 *         XAUTOCLAIM. An array containing all the successfully claimed messages
	 *         in the same format as XRANGE. An array containing message IDs that no
	 *         longer exist in the stream, and were deleted from the PEL in which
	 *         they were found.
	 */
	ClaimedMessages<byte[], byte[]> xautoclaim(byte[] key, XAutoClaimArgs<byte[]> args);

	List<StreamMessage<byte[], byte[]>> xclaim(byte[] key, byte[] group, byte[] consumerName, long minIdleTime,
			String... ids);

	/**
	 * <h1>Changes, or acquires, ownership of a message in a consumer group, as if
	 * the message was delivered a consumer group member.</h1><br>
	 * 
	 * XCLAIM key group consumer min-idle-time id [id ...] [IDLE ms]<br>
	 * [TIME unix-time-milliseconds] [RETRYCOUNT count] [FORCE] [JUSTID]<br>
	 * [LASTID lastid]<br>
	 * 
	 * 
	 * > XCLAIM mystream mygroup Alice 3600000 1526569498055-0<br>
	 * 1) 1) 1526569498055-0<br>
	 * 2) 1) "message"<br>
	 * 2) "orange"<br>
	 * 
	 * @return The command returns all the messages successfully claimed, in the
	 *         same format as XRANGE. However if the JUSTID option was specified,
	 *         only the message IDs are reported, without including the actual
	 *         message.
	 */
	List<StreamMessage<byte[], byte[]>> xclaim(byte[] key, byte[] group, byte[] consumerName, XClaimArgs args,
			String... ids);

	/**
	 * <h1>Returns the number of messages after removing them from a
	 * stream.</h1><br>
	 * 
	 * XDEL key id [id ...]
	 * 
	 * Removes the specified entries from a stream, and returns the number of
	 * entries deleted. This number may be less than the number of IDs passed to the
	 * command in the case where some of the specified IDs do not exist in the
	 * stream.
	 * 
	 * Normally you may think at a Redis stream as an append-only data structure,
	 * however Redis streams are represented in memory, so we are also able to
	 * delete entries. This may be useful, for instance, in order to comply with
	 * certain privacy policies.
	 * 
	 * > XADD mystream * a 1<br>
	 * 1538561698944-0<br>
	 * > XADD mystream * b 2<br>
	 * 1538561700640-0<br>
	 * > XADD mystream * c 3<br>
	 * 1538561701744-0<br>
	 * > XDEL mystream 1538561700640-0<br>
	 * (integer) 1<br>
	 * 127.0.0.1:6379> XRANGE mystream - +<br>
	 * 1) 1) 1538561698944-0<br>
	 * 2) 1) "a"<br>
	 * 2) "1"<br>
	 * 2) 1) 1538561701744-0<br>
	 * 2) 1) "c"<br>
	 * 2) "3"<br>
	 * 
	 * @return the number of entries actually deleted.
	 */
	long xdel(byte[] key, String... ids);

	String xgroupCreate(byte[] key, byte[] groupName, String id);

	/**
	 * <h1>Creates a consumer group.</h1><br>
	 * 
	 * XGROUP CREATE key group <id | $> [MKSTREAM] [ENTRIESREAD entries-read]
	 * 
	 * Create a new consumer group uniquely identified by <groupname> for the stream
	 * stored at <key>
	 * 
	 * Every group has a unique name in a given stream. When a consumer group with
	 * the same name already exists, the command returns a -BUSYGROUP error.
	 * 
	 * The command's <id> argument specifies the last delivered entry in the stream
	 * from the new group's perspective. The special ID $ is the ID of the last
	 * entry in the stream, but you can substitute it with any valid ID.
	 * 
	 * @param id $ is the ID of the last entry in the stream, > is lastConsumed
	 * 
	 * @return OK on success.
	 */
	String xgroupCreate(byte[] key, byte[] groupName, String id, XGroupCreateArgs args);

	/**
	 * <h1>Creates a consumer in a consumer group.</h1><br>
	 * 
	 * XGROUP CREATECONSUMER key group consumer
	 * 
	 * Create a consumer named <consumername> in the consumer group <groupname> of
	 * the stream that's stored at <key>.
	 * 
	 * Consumers are also created automatically whenever an operation, such as
	 * XREADGROUP, references a consumer that doesn't exist. This is valid for
	 * XREADGROUP only when there is data in the stream.
	 * 
	 * 
	 * @return the number of created consumers (0 or 1)
	 */
	boolean xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName);

	/**
	 * <h1>Deletes a consumer from a consumer group.</h1><br>
	 * 
	 * XGROUP DELCONSUMER key group consumer
	 * 
	 * The XGROUP DELCONSUMER command deletes a consumer from the consumer group.
	 * 
	 * Sometimes it may be useful to remove old consumers since they are no longer
	 * used.
	 * 
	 * Note, however, that any pending messages that the consumer had will become
	 * unclaimable after it was deleted. It is strongly recommended, therefore, that
	 * any pending messages are claimed or acknowledged prior to deleting the
	 * consumer from the group.
	 * 
	 * 
	 * @return the number of pending messages that the consumer had before it was
	 *         deleted
	 */
	long xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName);

	/**
	 * <h1>Destroys a consumer group.</h1><br>
	 * 
	 * XGROUP DESTROY key group
	 * 
	 * The XGROUP DESTROY command completely destroys a consumer group.
	 * 
	 * The consumer group will be destroyed even if there are active consumers, and
	 * pending messages, so make sure to call this command only when really needed.
	 * 
	 * 
	 * @return the number of destroyed consumer groups (0 or 1)
	 */
	long xgroupDestroy(byte[] key, byte[] groupName);

	/**
	 * <h1>Sets the last-delivered ID of a consumer group.</h1><br>
	 * 
	 * XGROUP SETID key group <id | $> [ENTRIESREAD entries-read]
	 * 
	 * Set the last delivered ID for a consumer group.
	 * 
	 * Normally, a consumer group's last delivered ID is set when the group is
	 * created with XGROUP CREATE. The XGROUP SETID command allows modifying the
	 * group's last delivered ID, without having to delete and recreate the group.
	 * For instance if you want the consumers in a consumer group to re-process all
	 * the messages in a stream, you may want to set its next ID to 0:
	 * 
	 * @param id
	 * 
	 * @return OK on success.
	 */
	String xgroupSetID(byte[] key, byte[] groupName, String id);

	/**
	 * <h1>Returns a list of the consumers in a consumer group.</h1><br>
	 * 
	 * XINFO CONSUMERS key group
	 * 
	 * This command returns the list of consumers that belong to the <groupname>
	 * consumer group of the stream stored at <key>.
	 * 
	 * The following information is provided for each consumer in the group:
	 * 
	 * name: the consumer's name pending: the number of entries in the PEL: pending
	 * messages for the consumer, which are messages that were delivered but are yet
	 * to be acknowledged idle: the number of milliseconds that have passed since
	 * the consumer's last attempted interaction (Examples: XREADGROUP, XCLAIM,
	 * XAUTOCLAIM) inactive: the number of milliseconds that have passed since the
	 * consumer's last successful interaction (Examples: XREADGROUP that actually
	 * read some entries into the PEL, XCLAIM/XAUTOCLAIM that actually claimed some
	 * entries)
	 * 
	 * > XINFO CONSUMERS mystream mygroup<br>
	 * 1) 1) name<br>
	 * 2) "Alice"<br>
	 * 3) pending<br>
	 * 4) (integer) 1<br>
	 * 5) idle<br>
	 * 6) (integer) 9104628<br>
	 * 7) inactive<br>
	 * 8) (integer) 18104698<br>
	 * 2) 1) name<br>
	 * 2) "Bob"<br>
	 * 3) pending<br>
	 * 4) (integer) 1<br>
	 * 5) idle<br>
	 * 6) (integer) 83841983<br>
	 * 7) inactive<br>
	 * 8) (integer) 993841998<br>
	 * 
	 * @return a list of consumers.
	 */
	List<Object> xinfoConsumers(byte[] key, byte[] group);

	/**
	 * <h1>Returns a list of the consumer groups of a stream.</h1><br>
	 * 
	 * XINFO GROUPS key
	 * 
	 * This command returns the list of all consumer groups of the stream stored at
	 * <key>.
	 * 
	 * By default, only the following information is provided for each of the
	 * groups:
	 * 
	 * name: the consumer group's name consumers: the number of consumers in the
	 * group pending: the length of the group's pending entries list (PEL), which
	 * are messages that were delivered but are yet to be acknowledged
	 * last-delivered-id: the ID of the last entry delivered to the group's
	 * consumers entries-read: the logical "read counter" of the last entry
	 * delivered to the group's consumers lag: the number of entries in the stream
	 * that are still waiting to be delivered to the group's consumers, or a NULL
	 * when that number can't be determined.
	 * 
	 * > XINFO GROUPS mystream<br>
	 * 1) 1) "name"<br>
	 * 2) "mygroup"<br>
	 * 3) "consumers"<br>
	 * 4) (integer) 2<br>
	 * 5) "pending"<br>
	 * 6) (integer) 2<br>
	 * 7) "last-delivered-id"<br>
	 * 8) "1638126030001-0"<br>
	 * 9) "entries-read"<br>
	 * 10) (integer) 2<br>
	 * 11) "lag"<br>
	 * 12) (integer) 0<br>
	 * 2) 1) "name"<br>
	 * 2) "some-other-group"<br>
	 * 3) "consumers"<br>
	 * 4) (integer) 1<br>
	 * 5) "pending"<br>
	 * 6) (integer) 0<br>
	 * 7) "last-delivered-id"<br>
	 * 8) "1638126028070-0"<br>
	 * 9) "entries-read"<br>
	 * 10) (integer) 1<br>
	 * 11) "lag"<br>
	 * 12) (integer) 1<br>
	 * 
	 * @return a list of consumer groups.
	 */
	List<Object> xinfoGroups(byte[] key);

	/**
	 * <h1>Returns information about a stream.</h1><br>
	 * 
	 * XINFO STREAM key [FULL [COUNT count]]
	 * 
	 * This command returns information about the stream stored at <key>.
	 * 
	 * The informative details provided by this command are:
	 * 
	 * length: the number of entries in the stream (see XLEN) radix-tree-keys: the
	 * number of keys in the underlying radix data structure radix-tree-nodes: the
	 * number of nodes in the underlying radix data structure groups: the number of
	 * consumer groups defined for the stream last-generated-id: the ID of the
	 * least-recently entry that was added to the stream max-deleted-entry-id: the
	 * maximal entry ID that was deleted from the stream entries-added: the count of
	 * all entries added to the stream during its lifetime first-entry: the ID and
	 * field-value tuples of the first entry in the stream last-entry: the ID and
	 * field-value tuples of the last entry in the stream The optional FULL modifier
	 * provides a more verbose reply. When provided, the FULL reply includes an
	 * entries array that consists of the stream entries (ID and field-value tuples)
	 * in ascending order. Furthermore, groups is also an array, and for each of the
	 * consumer groups it consists of the information reported by XINFO GROUPS and
	 * XINFO CONSUMERS.
	 * 
	 * The COUNT option can be used to limit the number of stream and PEL entries
	 * that are returned (The first <count> entries are returned). The default COUNT
	 * is 10 and a COUNT of 0 means that all entries will be returned (execution
	 * time may be long if the stream has a lot of entries).
	 * 
	 * Default reply:
	 * 
	 * > XINFO STREAM mystream<br>
	 * 1) "length"<br>
	 * 2) (integer) 2<br>
	 * 3) "radix-tree-keys"<br>
	 * 4) (integer) 1<br>
	 * 5) "radix-tree-nodes"<br>
	 * 6) (integer) 2<br>
	 * 7) "last-generated-id"<br>
	 * 8) "1638125141232-0"<br>
	 * 9) "max-deleted-entry-id"<br>
	 * 10) "0-0"<br>
	 * 11) "entries-added"<br>
	 * 12) (integer) 2<br>
	 * 13) "groups"<br>
	 * 14) (integer) 1<br>
	 * 15) "first-entry"<br>
	 * 16) 1) "1638125133432-0"<br>
	 * 2) 1) "message"<br>
	 * 2) "apple"<br>
	 * 17) "last-entry"<br>
	 * 18) 1) "1638125141232-0"<br>
	 * 2) 1) "message"<br>
	 * 2) "banana"<br>
	 * 
	 * Full reply:
	 * 
	 * > XADD mystream * foo bar<br>
	 * "1638125133432-0"<br>
	 * > XADD mystream * foo bar2<br>
	 * "1638125141232-0"<br>
	 * > XGROUP CREATE mystream mygroup 0-0<br>
	 * OK<br>
	 * > XREADGROUP GROUP mygroup Alice COUNT 1 STREAMS mystream ><br>
	 * 1) 1) "mystream"<br>
	 * 2) 1) 1) "1638125133432-0"<br>
	 * 2) 1) "foo"<br>
	 * 2) "bar"<br>
	 * > XINFO STREAM mystream FULL<br>
	 * 1) "length"<br>
	 * 2) (integer) 2<br>
	 * 3) "radix-tree-keys"<br>
	 * 4) (integer) 1<br>
	 * 5) "radix-tree-nodes"<br>
	 * 6) (integer) 2<br>
	 * 7) "last-generated-id"<br>
	 * 8) "1638125141232-0"<br>
	 * 9) "max-deleted-entry-id"<br>
	 * 10) "0-0"<br>
	 * 11) "entries-added"<br>
	 * 12) (integer) 2<br>
	 * 13) "entries"<br>
	 * 14) 1) 1) "1638125133432-0"<br>
	 * 2) 1) "foo"<br>
	 * 2) "bar"<br>
	 * 2) 1) "1638125141232-0"<br>
	 * 2) 1) "foo"<br>
	 * 2) "bar2"<br>
	 * 15) "groups"<br>
	 * 16) 1) 1) "name"<br>
	 * 2) "mygroup"<br>
	 * 3) "last-delivered-id"<br>
	 * 4) "1638125133432-0"<br>
	 * 5) "entries-read"<br>
	 * 6) (integer) 1<br>
	 * 7) "lag"<br>
	 * 8) (integer) 1<br>
	 * 9) "pel-count"<br>
	 * 10) (integer) 1<br>
	 * 11) "pending"<br>
	 * 12) 1) 1) "1638125133432-0"<br>
	 * 2) "Alice"<br>
	 * 3) (integer) 1638125153423<br>
	 * 4) (integer) 1<br>
	 * 13) "consumers"<br>
	 * 14) 1) 1) "name"<br>
	 * 2) "Alice"<br>
	 * 3) "seen-time"<br>
	 * 4) (integer) 1638125133422<br>
	 * 5) "active-time"<br>
	 * 6) (integer) 1638125133432<br>
	 * 7) "pel-count"<br>
	 * 8) (integer) 1<br>
	 * 9) "pending"<br>
	 * 10) 1) 1) "1638125133432-0"<br>
	 * 2) (integer) 1638125133432<br>
	 * 3) (integer) 1<br>
	 * 
	 * @return a list of informational bits
	 */
	List<Object> xinfoStream(byte[] key);

	/**
	 * <h1>Return the number of messages in a stream.</h1><br>
	 * 
	 * XLEN key
	 * 
	 * Returns the number of entries inside a stream. If the specified key does not
	 * exist the command returns zero, as if the stream was empty. However note that
	 * unlike other Redis types, zero-length streams are possible, so you should
	 * call TYPE or EXISTS in order to check if a key exists or not.
	 * 
	 * Streams are not auto-deleted once they have no entries inside (for instance
	 * after an XDEL call), because the stream may have consumer groups associated
	 * with it.
	 * 
	 * redis> XADD mystream * item 1<br>
	 * "1689300733075-0"<br>
	 * redis> XADD mystream * item 2<br>
	 * "1689300733076-0"<br>
	 * redis> XADD mystream * item 3<br>
	 * "1689300733076-1"<br>
	 * redis> XLEN mystream<br>
	 * (integer) 3<br>
	 * redis> <br>
	 * 
	 * 
	 * @return the number of entries of the stream at key.
	 */
	long xlen(byte[] key);

	PendingMessages xpending(byte[] key, byte[] groupName);

	List<PendingMessage> xpending(byte[] key, byte[] groupName, Range<String> range, Limit limit);

	List<PendingMessage> xpending(byte[] key, byte[] groupName, byte[] consumerName, Range<String> range, Limit limit);

	/**
	 * <h1>Returns the information and entries from a stream consumer group's
	 * pending entries list.</h1><br>
	 * 
	 * XPENDING key group [[IDLE min-idle-time] start end count [consumer]]
	 * 
	 * Fetching data from a stream via a consumer group, and not acknowledging such
	 * data, has the effect of creating pending entries. This is well explained in
	 * the XREADGROUP command, and even better in our introduction to Redis Streams.
	 * The XACK command will immediately remove the pending entry from the Pending
	 * Entries List (PEL) since once a message is successfully processed, there is
	 * no longer need for the consumer group to track it and to remember the current
	 * owner of the message.
	 * 
	 * The XPENDING command is the interface to inspect the list of pending
	 * messages, and is as thus a very important command in order to observe and
	 * understand what is happening with a streams consumer groups: what clients are
	 * active, what messages are pending to be consumed, or to see if there are idle
	 * messages. Moreover this command, together with XCLAIM is used in order to
	 * implement recovering of consumers that are failing for a long time, and as a
	 * result certain messages are not processed: a different consumer can claim the
	 * message and continue. This is better explained in the streams intro and in
	 * the XCLAIM command page, and is not covered here.
	 * 
	 * 
	 * 
	 * @return Array reply, specifically:
	 * 
	 *         The command returns data in different format depending on the way it
	 *         is called, as previously explained in this page. However the reply is
	 *         always an array of items.
	 */
	List<PendingMessage> xpending(byte[] key, XPendingArgs<byte[]> args);

	List<StreamMessage<byte[], byte[]>> xrange(byte[] key, byte[] start, byte[] end);

	/**
	 * <h1>Returns the messages from a stream within a range of IDs.</h1><br>
	 * 
	 * XRANGE key start end [COUNT count]
	 * 
	 * The command returns the stream entries matching a given range of IDs. The
	 * range is specified by a minimum and maximum ID. All the entries having an ID
	 * between the two specified or exactly one of the two IDs specified (closed
	 * interval) are returned.
	 * 
	 * The XRANGE command has a number of applications:
	 * 
	 * Returning items in a specific time range. This is possible because Stream IDs
	 * are related to time. Iterating a stream incrementally, returning just a few
	 * items at every iteration. However it is semantically much more robust than
	 * the SCAN family of functions. Fetching a single entry from a stream,
	 * providing the ID of the entry to fetch two times: as start and end of the
	 * query interval. The command also has a reciprocal command returning items in
	 * the reverse order, called XREVRANGE, which is otherwise identical.
	 * 
	 * redis> XADD writers * name Virginia surname Woolf<br>
	 * "1689303635721-0"<br>
	 * redis> XADD writers * name Jane surname Austen<br>
	 * "1689303635721-1"<br>
	 * redis> XADD writers * name Toni surname Morrison<br>
	 * "1689303635721-2"<br>
	 * redis> XADD writers * name Agatha surname Christie<br>
	 * "1689303635722-0"<br>
	 * redis> XADD writers * name Ngozi surname Adichie<br>
	 * "1689303635722-1"<br>
	 * redis> XLEN writers<br>
	 * (integer) 5<br>
	 * redis> XRANGE writers - + COUNT 2<br>
	 * 1) 1) "1689303635721-0"<br>
	 * 2) 1) "name"<br>
	 * 2) "Virginia"<br>
	 * 3) "surname"<br>
	 * 4) "Woolf"<br>
	 * 2) 1) "1689303635721-1"<br>
	 * 2) 1) "name"<br>
	 * 2) "Jane"<br>
	 * 3) "surname"<br>
	 * 4) "Austen"<br>
	 * redis> <br>
	 * 
	 * 
	 * @return Array reply, specifically:
	 * 
	 *         The command returns the entries with IDs matching the specified
	 *         range. The returned entries are complete, that means that the ID and
	 *         all the fields they are composed are returned. Moreover, the entries
	 *         are returned with their fields and values in the exact same order as
	 *         XADD added them.
	 */
	List<StreamMessage<byte[], byte[]>> xrange(byte[] key, byte[] start, byte[] end, int count);

	List<StreamMessage<byte[], byte[]>> xread(List<XReadArgs.StreamOffset<byte[]>> streams);

	/**
	 * <h1>Returns messages from multiple streams with IDs greater than the ones
	 * requested. Blocks until a message is available otherwise.</h1><br>
	 * 
	 * XREAD [COUNT count] [BLOCK milliseconds] STREAMS key [key ...] id [id ...]
	 * 
	 * Read data from one or multiple streams, only returning entries with an ID
	 * greater than the last received ID reported by the caller. This command has an
	 * option to block if items are not available, in a similar fashion to BRPOP or
	 * BZPOPMIN and others.
	 * 
	 * > XREAD COUNT 2 STREAMS mystream writers 0-0 0-0<br>
	 * 1) 1) "mystream"<br>
	 * 2) 1) 1) 1526984818136-0<br>
	 * 2) 1) "duration"<br>
	 * 2) "1532"<br>
	 * 3) "event-id"<br>
	 * 4) "5"<br>
	 * 5) "user-id"<br>
	 * 6) "7782813"<br>
	 * 2) 1) 1526999352406-0<br>
	 * 2) 1) "duration"<br>
	 * 2) "812"<br>
	 * 3) "event-id"<br>
	 * 4) "9"<br>
	 * 5) "user-id"<br>
	 * 6) "388234"<br>
	 * 2) 1) "writers"<br>
	 * 2) 1) 1) 1526985676425-0<br>
	 * 2) 1) "name"<br>
	 * 2) "Virginia"<br>
	 * 3) "surname"<br>
	 * 4) "Woolf"<br>
	 * 2) 1) 1526985685298-0<br>
	 * 2) 1) "name"<br>
	 * 2) "Jane"<br>
	 * 3) "surname"<br>
	 * 4) "Austen"<br>
	 * 
	 * 
	 * @return Array reply, specifically:
	 * 
	 *         The command returns an array of results: each element of the returned
	 *         array is an array composed of a two element containing the key name
	 *         and the entries reported for that key. The entries reported are full
	 *         stream entries, having IDs and the list of all the fields and values.
	 *         Field and values are guaranteed to be reported in the same order they
	 *         were added by XADD.
	 * 
	 *         When BLOCK is used, on timeout a null reply is returned.
	 * 
	 *         Reading the Redis Streams introduction is highly suggested in order
	 *         to understand more about the streams overall behavior and semantics.
	 */
	List<StreamMessage<byte[], byte[]>> xread(XReadArgs args, List<XReadArgs.StreamOffset<byte[]>> streams);

	List<StreamMessage<byte[], byte[]>> xreadGroup(byte[] groupName, byte[] consumerName,
			List<XReadArgs.StreamOffset<byte[]>> streams);

	/**
	 * <h1>Returns new or historical messages from a stream for a consumer in a
	 * group. Blocks until a message is available otherwise.</h1><br>
	 * 
	 * XREADGROUP GROUP group consumer [COUNT count] [BLOCK milliseconds] [NOACK]
	 * STREAMS key [key ...] id [id ...]
	 * 
	 * The XREADGROUP command is a special version of the XREAD command with support
	 * for consumer groups. Probably you will have to understand the XREAD command
	 * before reading this page will makes sense.
	 * 
	 * Moreover, if you are new to streams, we recommend to read our introduction to
	 * Redis Streams. Make sure to understand the concept of consumer group in the
	 * introduction so that following how this command works will be simpler.
	 * 
	 * 
	 * @return Array reply, specifically:
	 * 
	 *         The command returns an array of results: each element of the returned
	 *         array is an array composed of a two element containing the key name
	 *         and the entries reported for that key. The entries reported are full
	 *         stream entries, having IDs and the list of all the fields and values.
	 *         Field and values are guaranteed to be reported in the same order they
	 *         were added by XADD.
	 * 
	 *         When BLOCK is used, on timeout a null reply is returned.
	 * 
	 *         Reading the Redis Streams introduction is highly suggested in order
	 *         to understand more about the streams overall behavior and semantics.
	 */
	List<StreamMessage<byte[], byte[]>> xreadGroup(byte[] groupName, byte[] consumerName, XReadArgs args,
			List<XReadArgs.StreamOffset<byte[]>> streams);

	List<StreamMessage<byte[], byte[]>> xrevrange(byte[] key, byte[] end, byte[] start);

	/**
	 * <h1>Returns the messages from a stream within a range of IDs in reverse
	 * order.</h1><br>
	 * 
	 * XREVRANGE key end start [COUNT count]
	 * 
	 * This command is exactly like XRANGE, but with the notable difference of
	 * returning the entries in reverse order, and also taking the start-end range
	 * in reverse order: in XREVRANGE you need to state the end ID and later the
	 * start ID, and the command will produce all the element between (or exactly
	 * like) the two IDs, starting from the end side.
	 * 
	 * redis> XADD writers * name Virginia surname Woolf<br>
	 * "1689311731051-0"<br>
	 * redis> XADD writers * name Jane surname Austen<br>
	 * "1689311731051-1"<br>
	 * redis> XADD writers * name Toni surname Morrison<br>
	 * "1689311731051-2"<br>
	 * redis> XADD writers * name Agatha surname Christie<br>
	 * "1689311731052-0"<br>
	 * redis> XADD writers * name Ngozi surname Adichie<br>
	 * "1689311731052-1"<br>
	 * redis> XLEN writers<br>
	 * (integer) 5<br>
	 * redis> XREVRANGE writers + - COUNT 1<br>
	 * 1) 1) "1689311731052-1"<br>
	 * 2) 1) "name"<br>
	 * 2) "Ngozi"<br>
	 * 3) "surname"<br>
	 * 4) "Adichie"<br>
	 * redis> <br>
	 * 
	 * @return Array reply, specifically:
	 * 
	 *         The command returns the entries with IDs matching the specified
	 *         range, from the higher ID to the lower ID matching. The returned
	 *         entries are complete, that means that the ID and all the fields they
	 *         are composed are returned. Moreover the entries are returned with
	 *         their fields and values in the exact same order as XADD added them.
	 */
	List<StreamMessage<byte[], byte[]>> xrevrange(byte[] key, byte[] end, byte[] start, int count);

	long xtrim(byte[] key, long maxLen, boolean approximateLength);

	/**
	 * <h1>Deletes messages from the beginning of a stream.</h1><br>
	 * 
	 * XTRIM key <MAXLEN | MINID> [= | ~] threshold [LIMIT count]
	 * 
	 * XTRIM trims the stream by evicting older entries (entries with lower IDs) if
	 * needed.
	 * 
	 * Trimming the stream can be done using one of these strategies:
	 * 
	 * MAXLEN: Evicts entries as long as the stream's length exceeds the specified
	 * threshold, where threshold is a positive integer. MINID: Evicts entries with
	 * IDs lower than threshold, where threshold is a stream ID.
	 * 
	 * redis> XADD mystream * field1 A field2 B field3 C field4 D<br>
	 * "1689311910598-0"<br>
	 * redis> XTRIM mystream MAXLEN 2<br>
	 * (integer) 0<br>
	 * redis> XRANGE mystream - +<br>
	 * 1) 1) "1689311910598-0"<br>
	 * 2) 1) "field1"<br>
	 * 2) "A"<br>
	 * 3) "field2"<br>
	 * 4) "B"<br>
	 * 5) "field3"<br>
	 * 6) "C"<br>
	 * 7) "field4"<br>
	 * 8) "D"<br>
	 * redis> <br>
	 * 
	 * 
	 * @return Integer reply: The number of entries deleted from the stream.
	 */
	long xtrim(byte[] key, XTrimArgs args);

}