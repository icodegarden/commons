package io.github.icodegarden.commons.redis;

import java.util.List;
import java.util.Map;

import io.github.icodegarden.commons.redis.args.ClaimedMessages;
import io.github.icodegarden.commons.redis.args.StreamMessage;
import io.github.icodegarden.commons.redis.args.XAddArgs;
import io.github.icodegarden.commons.redis.args.XAutoClaimArgs;
import io.github.icodegarden.commons.redis.args.XClaimArgs;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.params.XReadGroupParams;
import redis.clients.jedis.params.XReadParams;
import redis.clients.jedis.params.XTrimParams;

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

	byte[] xadd(byte[] key, Map<byte[], byte[]> hash);

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
	byte[] xadd(byte[] key, XAddArgs args, Map<byte[], byte[]> hash);

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
	 * @return OK on success.
	 */
	String xgroupCreate(byte[] key, byte[] groupName, byte[] id, boolean makeStream);

	long xlen(byte[] key);

	List<byte[]> xrange(byte[] key, byte[] start, byte[] end);

	List<byte[]> xrange(byte[] key, byte[] start, byte[] end, int count);

	List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start);

	List<byte[]> xrevrange(byte[] key, byte[] end, byte[] start, int count);

	String xgroupSetID(byte[] key, byte[] groupName, byte[] id);

	long xgroupDestroy(byte[] key, byte[] groupName);

	boolean xgroupCreateConsumer(byte[] key, byte[] groupName, byte[] consumerName);

	long xgroupDelConsumer(byte[] key, byte[] groupName, byte[] consumerName);

	long xtrim(byte[] key, long maxLen, boolean approximateLength);

	long xtrim(byte[] key, XTrimParams params);

	Object xpending(byte[] key, byte[] groupName);

	/**
	 * @deprecated Use
	 *             {@link StreamBinaryCommands#xpending(byte[], byte[], redis.clients.jedis.params.XPendingParams)}.
	 */
	@Deprecated
	List<Object> xpending(byte[] key, byte[] groupName, byte[] start, byte[] end, int count, byte[] consumerName);

	List<Object> xpending(byte[] key, byte[] groupName, XPendingParams params);

	Object xinfoStream(byte[] key);

	/**
	 * Introspection command used in order to retrieve all information about the
	 * stream
	 * 
	 * @param key Stream name
	 */
	Object xinfoStreamFull(byte[] key);

	/**
	 * Introspection command used in order to retrieve all information about the
	 * stream
	 * 
	 * @param key   Stream name
	 * @param count stream info count
	 */
	Object xinfoStreamFull(byte[] key, int count);

	/**
	 * @deprecated Use {@link StreamBinaryCommands#xinfoGroups(byte[])}.
	 */
	@Deprecated
	List<Object> xinfoGroup(byte[] key);

	List<Object> xinfoGroups(byte[] key);

	List<Object> xinfoConsumers(byte[] key, byte[] group);

	List<byte[]> xread(XReadParams xReadParams, Map.Entry<byte[], byte[]>... streams);

	List<byte[]> xreadGroup(byte[] groupName, byte[] consumer, XReadGroupParams xReadGroupParams,
			Map.Entry<byte[], byte[]>... streams);

}