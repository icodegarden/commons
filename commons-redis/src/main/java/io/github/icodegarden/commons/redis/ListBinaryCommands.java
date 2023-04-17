//package io.github.icodegarden.commons.redis;
//
//import java.util.List;
//
//import redis.clients.jedis.args.ListDirection;
//import redis.clients.jedis.args.ListPosition;
//import redis.clients.jedis.params.LPosParams;
//import redis.clients.jedis.util.KeyValue;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public interface ListBinaryCommands {
//
//	/**
//	 * <h1>从指定的list中pop元素，并push到另一个list。pop时阻塞直到有元素或超时，当没有元素时删除list</h1><br>
//	 * 
//	 * BLMOVE is the blocking variant of LMOVE. When source contains elements, this
//	 * command behaves exactly like LMOVE. When used inside a MULTI/EXEC block, this
//	 * command behaves exactly like LMOVE. When source is empty, Redis will block
//	 * the connection until another client pushes to it or until timeout (a double
//	 * value specifying the maximum number of seconds to block) is reached. A
//	 * timeout of zero can be used to block indefinitely.
//	 * 
//	 * This command comes in place of the now deprecated BRPOPLPUSH. Doing BLMOVE
//	 * RIGHT LEFT is equivalent.
//	 * 
//	 * 语法：BLMOVE source destination <LEFT | RIGHT> <LEFT | RIGHT> timeout
//	 * 
//	 * @param srcKey
//	 * @param dstKey
//	 * @param from
//	 * @param to
//	 * @param timeout
//	 * @return
//	 */
//	byte[] blmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to, double timeout);
//
//	KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, byte[]... keys);
//
//	/**
//	 * <h1>从1个或多个list中pop指定数量的元素。pop时阻塞直到有元素或超时，当没有元素时删除list</h1><br>
//	 * 
//	 * BLMPOP is the blocking variant of LMPOP.
//	 * 
//	 * When any of the lists contains elements, this command behaves exactly like
//	 * LMPOP. When used inside a MULTI/EXEC block, this command behaves exactly like
//	 * LMPOP. When all lists are empty, Redis will block the connection until
//	 * another client pushes to it or until the timeout (a double value specifying
//	 * the maximum number of seconds to block) elapses. A timeout of zero can be
//	 * used to block indefinitely.
//	 * 
//	 * See LMPOP for more information.
//	 * 
//	 * @param timeout
//	 * @param direction
//	 * @param count
//	 * @param keys
//	 * @return
//	 */
//	KeyValue<byte[], List<byte[]>> blmpop(long timeout, ListDirection direction, int count, byte[]... keys);
//
//	List<byte[]> blpop(int timeout, byte[]... keys);
//
//	/**
//	 * <h1>从1个或多个list中pop一个元素。pop时阻塞直到有元素或超时，当没有元素时删除list</h1><br>
//	 * 
//	 * BLPOP is a blocking list pop primitive. It is the blocking version of LPOP
//	 * because it blocks the connection when there are no elements to pop from any
//	 * of the given lists. An element is popped from the head of the first list that
//	 * is non-empty, with the given keys being checked in the order that they are
//	 * given.
//	 * 
//	 * redis> DEL list1 list2<br>
//	 * (integer) 0<br>
//	 * redis> RPUSH list1 a b c<br>
//	 * (integer) 3<br>
//	 * redis> BLPOP list1 list2 0<br>
//	 * 1) "list1"<br>
//	 * 2) "a"<br>
//	 * 
//	 * @param timeout 0表示非阻塞
//	 * @param keys
//	 * @return
//	 */
//	List<byte[]> blpop(double timeout, byte[]... keys);
//
//	List<byte[]> brpop(int timeout, byte[]... keys);
//
//	/**
//	 * <h1>从1个或多个list中pop一个元素。pop时阻塞直到有元素或超时，当没有元素时删除list</h1><br>
//	 * 
//	 * BLPOP is a blocking list pop primitive. It is the blocking version of LPOP
//	 * because it blocks the connection when there are no elements to pop from any
//	 * of the given lists. An element is popped from the head of the first list that
//	 * is non-empty, with the given keys being checked in the order that they are
//	 * given.
//	 * 
//	 * redis> DEL list1 list2<br>
//	 * (integer) 0<br>
//	 * redis> RPUSH list1 a b c<br>
//	 * (integer) 3<br>
//	 * redis> BRPOP list1 list2 0<br>
//	 * 1) "list1"<br>
//	 * 2) "c"<br>
//	 * 
//	 * @param timeout 0表示非阻塞
//	 * @param keys
//	 * @return
//	 */
//	List<byte[]> brpop(double timeout, byte[]... keys);
//
//	/**
//	 * <h1>从1个list中pop元素，并push到另一个list然后返回。pop时阻塞直到有元素或超时，当没有元素时删除list</h1><br>
//	 * 
//	 * BRPOPLPUSH is the blocking variant of RPOPLPUSH. When source contains
//	 * elements, this command behaves exactly like RPOPLPUSH. When used inside a
//	 * MULTI/EXEC block, this command behaves exactly like RPOPLPUSH. When source is
//	 * empty, Redis will block the connection until another client pushes to it or
//	 * until timeout is reached. A timeout of zero can be used to block
//	 * indefinitely.
//	 * 
//	 * See RPOPLPUSH for more information.
//	 * 
//	 * 语法：BRPOPLPUSH source destination timeout
//	 * 
//	 * @param source
//	 * @param destination
//	 * @param timeout
//	 * @return
//	 */
//	byte[] brpoplpush(final byte[] source, final byte[] destination, final int timeout);
//
//	/**
//	 * <h1>返回对应index的元素</h1><br>
//	 * 
//	 * Returns the element at index index in the list stored at key. The index is
//	 * zero-based, so 0 means the first element, 1 the second element and so on.
//	 * Negative indices can be used to designate elements starting at the tail of
//	 * the list. Here, -1 means the last element, -2 means the penultimate and so
//	 * forth.
//	 * 
//	 * When the value at key is not a list, an error is returned.
//	 * 
//	 * redis> LPUSH mylist "World"<br>
//	 * (integer) 1<br>
//	 * redis> LPUSH mylist "Hello"<br>
//	 * (integer) 2<br>
//	 * redis> LINDEX mylist 0<br>
//	 * "Hello"<br>
//	 * redis> LINDEX mylist -1<br>
//	 * "World"<br>
//	 * redis> LINDEX mylist 3<br>
//	 * (nil)<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param index
//	 * @return
//	 */
//	byte[] lindex(final byte[] key, final long index);
//
//	/**
//	 * <h1>插入一个元素</h1><br>
//	 * 
//	 * Inserts element in the list stored at key either before or after the
//	 * reference value pivot.
//	 * 
//	 * When key does not exist, it is considered an empty list and no operation is
//	 * performed.
//	 * 
//	 * An error is returned when key exists but does not hold a list value.
//	 * 
//	 * redis> RPUSH mylist "Hello"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "World"<br>
//	 * (integer) 2<br>
//	 * redis> LINSERT mylist BEFORE "World" "There"<br>
//	 * (integer) 3<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "Hello"<br>
//	 * 2) "There"<br>
//	 * 3) "World"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param where
//	 * @param pivot
//	 * @param value
//	 * @return
//	 */
//	Long linsert(final byte[] key, final ListPosition where, final byte[] pivot, final byte[] value);
//
//	/**
//	 * <h1>返回list的size</h1><br>
//	 * 
//	 * Returns the length of the list stored at key. If key does not exist, it is
//	 * interpreted as an empty list and 0 is returned. An error is returned when the
//	 * value stored at key is not a list.
//	 * 
//	 * redis> LPUSH mylist "World"<br>
//	 * (integer) 1<br>
//	 * redis> LPUSH mylist "Hello"<br>
//	 * (integer) 2<br>
//	 * redis> LLEN mylist<br>
//	 * (integer) 2<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @return
//	 */
//	Long llen(final byte[] key);
//
//	/**
//	 * <h1>从指定的list中pop元素，如果有元素再push到另一个list。当没有元素时删除list</h1><br>
//	 * 
//	 * Atomically returns and removes the first/last element (head/tail depending on
//	 * the wherefrom argument) of the list stored at source, and pushes the element
//	 * at the first/last element (head/tail depending on the whereto argument) of
//	 * the list stored at destination.
//	 * 
//	 * For example: consider source holding the list a,b,c, and destination holding
//	 * the list x,y,z. Executing LMOVE source destination RIGHT LEFT results in
//	 * source holding a,b and destination holding c,x,y,z.
//	 * 
//	 * If source does not exist, the value nil is returned and no operation is
//	 * performed. If source and destination are the same, the operation is
//	 * equivalent to removing the first/last element from the list and pushing it as
//	 * first/last element of the list, so it can be considered as a list rotation
//	 * command (or a no-op if wherefrom is the same as whereto).
//	 * 
//	 * This command comes in place of the now deprecated RPOPLPUSH. Doing LMOVE
//	 * RIGHT LEFT is equivalent.
//	 * 
//	 * redis> RPUSH mylist "one"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "two"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "three"<br>
//	 * (integer) 3<br>
//	 * redis> LMOVE mylist myotherlist RIGHT LEFT<br>
//	 * "three"<br>
//	 * redis> LMOVE mylist myotherlist LEFT RIGHT<br>
//	 * "one"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "two"<br>
//	 * redis> LRANGE myotherlist 0 -1<br>
//	 * 1) "three"<br>
//	 * 2) "one"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * 
//	 * @param srcKey
//	 * @param dstKey
//	 * @param from
//	 * @param to
//	 * @return
//	 */
//	byte[] lmove(byte[] srcKey, byte[] dstKey, ListDirection from, ListDirection to);
//
//	KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, byte[]... keys);
//
//	/**
//	 * <h1>从指定的list中pop指定数量的元素。当没有元素时删除list</h1><br>
//	 * 
//	 * Pops one or more elements from the first non-empty list key from the list of
//	 * provided key names.
//	 * 
//	 * LMPOP and BLMPOP are similar to the following, more limited, commands:
//	 * 
//	 * LPOP or RPOP which take only one key, and can return multiple elements. BLPOP
//	 * or BRPOP which take multiple keys, but return only one element from just one
//	 * key. See BLMPOP for the blocking variant of this command.
//	 * 
//	 * Elements are popped from either the left or right of the first non-empty list
//	 * based on the passed argument. The number of returned elements is limited to
//	 * the lower between the non-empty list's length, and the count argument (which
//	 * defaults to 1).
//	 * 
//	 * redis> LMPOP 2 non1 non2 LEFT COUNT 10<br>
//	 * (error) object of type 'NoneType' has no len()<br>
//	 * redis> LPUSH mylist "one" "two" "three" "four" "five"<br>
//	 * (integer) 5<br>
//	 * redis> LMPOP 1 mylist LEFT<br>
//	 * 1) "mylist"<br>
//	 * 2) 1) "five"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "four"<br>
//	 * 2) "three"<br>
//	 * 3) "two"<br>
//	 * 4) "one"<br>
//	 * redis> LMPOP 1 mylist RIGHT COUNT 10<br>
//	 * 1) "mylist"<br>
//	 * 2) 1) "one"<br>
//	 * 2) "two"<br>
//	 * 3) "three"<br>
//	 * 4) "four"<br>
//	 * redis> LPUSH mylist "one" "two" "three" "four" "five"<br>
//	 * (integer) 5<br>
//	 * redis> LPUSH mylist2 "a" "b" "c" "d" "e"<br>
//	 * (integer) 5<br>
//	 * redis> LMPOP 2 mylist mylist2 right count 3<br>
//	 * 1) "mylist"<br>
//	 * 2) 1) "one"<br>
//	 * 2) "two"<br>
//	 * 3) "three"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "five"<br>
//	 * 2) "four"<br>
//	 * redis> LMPOP 2 mylist mylist2 right count 5<br>
//	 * 1) "mylist"<br>
//	 * 2) 1) "four"<br>
//	 * 2) "five"<br>
//	 * redis> LMPOP 2 mylist mylist2 right count 10<br>
//	 * 1) "mylist2"<br>
//	 * 2) 1) "a"<br>
//	 * 2) "b"<br>
//	 * 3) "c"<br>
//	 * 4) "d"<br>
//	 * 5) "e"<br>
//	 * redis> EXISTS mylist mylist2<br>
//	 * (integer) 0<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param direction
//	 * @param count
//	 * @param keys
//	 * @return
//	 */
//	KeyValue<byte[], List<byte[]>> lmpop(ListDirection direction, int count, byte[]... keys);
//
//	byte[] lpop(final byte[] key);
//
//	/**
//	 * 
//	 * <h1>从指定的list中pop指定数量的元素。当没有元素时删除list</h1><br>
//	 * 
//	 * Removes and returns the first elements of the list stored at key.
//	 * 
//	 * By default, the command pops a single element from the beginning of the list.
//	 * When provided with the optional count argument, the reply will consist of up
//	 * to count elements, depending on the list's length.
//	 * 
//	 * redis> RPUSH mylist "one" "two" "three" "four" "five"<br>
//	 * (integer) 5<br>
//	 * redis> LPOP mylist<br>
//	 * "one"<br>
//	 * redis> LPOP mylist 2<br>
//	 * 1) "two"<br>
//	 * 2) "three"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "four"<br>
//	 * 2) "five"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param count
//	 * @return
//	 */
//	List<byte[]> lpop(final byte[] key, final int count);
//
//	Long lpos(final byte[] key, final byte[] element);
//
//	Long lpos(final byte[] key, final byte[] element, final LPosParams params);
//
//	/**
//	 * <h1>返回元素所在的index</h1><br>
//	 * 
//	 * redis> RPUSH mylist a b c d 1 2 3 4 3 3 3<br>
//	 * (integer) 11<br>
//	 * redis> LPOS mylist 3<br>
//	 * (integer) 6<br>
//	 * redis> LPOS mylist 3 COUNT 0 RANK 2<br>
//	 * 1) (integer) 8<br>
//	 * 2) (integer) 9<br>
//	 * 3) (integer) 10<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param element
//	 * @param params
//	 * @param count
//	 * @return
//	 */
//	List<Long> lpos(final byte[] key, final byte[] element, final LPosParams params, final long count);
//
//	/**
//	 * <h1>push1个或多个元素，key不存在则创建</h1><br>
//	 * 
//	 * Insert all the specified values at the head of the list stored at key. If key
//	 * does not exist, it is created as empty list before performing the push
//	 * operations. When key holds a value that is not a list, an error is returned.
//	 * 
//	 * It is possible to push multiple elements using a single command call just
//	 * specifying multiple arguments at the end of the command. Elements are
//	 * inserted one after the other to the head of the list, from the leftmost
//	 * element to the rightmost element. So for instance the command LPUSH mylist a
//	 * b c will result into a list containing c as first element, b as second
//	 * element and a as third element.
//	 * 
//	 * redis> LPUSH mylist "world"<br>
//	 * (integer) 1<br>
//	 * redis> LPUSH mylist "hello"<br>
//	 * (integer) 2<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "hello"<br>
//	 * 2) "world"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param strings
//	 * @return
//	 */
//	Long lpush(final byte[] key, final byte[]... strings);
//
//	/**
//	 * <h1>push1个或多个元素，只有当list存在时才可以</h1><br>
//	 * 
//	 * Inserts specified values at the head of the list stored at key, only if key
//	 * already exists and holds a list. In contrary to LPUSH, no operation will be
//	 * performed when key does not yet exist.
//	 * 
//	 * redis> LPUSH mylist "World"<br>
//	 * (integer) 1<br>
//	 * redis> LPUSHX mylist "Hello"<br>
//	 * (integer) 2<br>
//	 * redis> LPUSHX myotherlist "Hello"<br>
//	 * (integer) 0<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "Hello"<br>
//	 * 2) "World"<br>
//	 * redis> LRANGE myotherlist 0 -1<br>
//	 * (empty array)<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param string
//	 * @return
//	 */
//	Long lpushx(final byte[] key, final byte[]... string);
//
//	/**
//	 * <h1>返回指定范围内的元素</h1><br>
//	 * 
//	 * Returns the specified elements of the list stored at key. The offsets start
//	 * and stop are zero-based indexes, with 0 being the first element of the list
//	 * (the head of the list), 1 being the next element and so on.
//	 * 
//	 * These offsets can also be negative numbers indicating offsets starting at the
//	 * end of the list. For example, -1 is the last element of the list, -2 the
//	 * penultimate, and so on.
//	 * 
//	 * redis> RPUSH mylist "one"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "two"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "three"<br>
//	 * (integer) 3<br>
//	 * redis> LRANGE mylist 0 0<br>
//	 * 1) "one"<br>
//	 * redis> LRANGE mylist -3 2<br>
//	 * 1) "one"<br>
//	 * 2) "two"<br>
//	 * 3) "three"<br>
//	 * redis> LRANGE mylist -100 100<br>
//	 * 1) "one"<br>
//	 * 2) "two"<br>
//	 * 3) "three"<br>
//	 * redis> LRANGE mylist 5 10<br>
//	 * (empty array)<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param start
//	 * @param stop
//	 * @return
//	 */
//	List<byte[]> lrange(final byte[] key, final long start, final long stop);
//
//	/**
//	 * <h1>删除指定的1个或多个元素</h1><br>
//	 * 
//	 * Removes the first count occurrences of elements equal to element from the
//	 * list stored at key. The count argument influences the operation in the
//	 * following ways:
//	 * 
//	 * count > 0: Remove elements equal to element moving from head to tail. count <
//	 * 0: Remove elements equal to element moving from tail to head. count = 0:
//	 * Remove all elements equal to element. For example, LREM list -2 "hello" will
//	 * remove the last two occurrences of "hello" in the list stored at list.
//	 * 
//	 * Note that non-existing keys are treated like empty lists, so when key does
//	 * not exist, the command will always return 0.
//	 * 
//	 * redis> RPUSH mylist "hello"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "hello"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "foo"<br>
//	 * (integer) 3<br>
//	 * redis> RPUSH mylist "hello"<br>
//	 * (integer) 4<br>
//	 * redis> LREM mylist -2 "hello"<br>
//	 * (integer) 2<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "hello"<br>
//	 * 2) "foo"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param count
//	 * @param value
//	 * @return
//	 */
//	Long lrem(final byte[] key, final long count, final byte[] value);
//
//	/**
//	 * <h1>在指定的index位置上设置元素</h1><br>
//	 * 
//	 * Sets the list element at index to element. For more information on the index
//	 * argument, see LINDEX.
//	 * 
//	 * An error is returned for out of range indexes.
//	 * 
//	 * redis> RPUSH mylist "one"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "two"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "three"<br>
//	 * (integer) 3<br>
//	 * redis> LSET mylist 0 "four"<br>
//	 * "OK"<br>
//	 * redis> LSET mylist -2 "five"<br>
//	 * "OK"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "four"<br>
//	 * 2) "five"<br>
//	 * 3) "three"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param key
//	 * @param index
//	 * @param value
//	 * @return
//	 */
//	String lset(final byte[] key, final long index, final byte[] value);
//
//	/**
//	 * <h1>让list只保留指定范围的元素</h1><br>
//	 * 
//	 * Trim an existing list so that it will contain only the specified range of
//	 * elements specified. Both start and stop are zero-based indexes, where 0 is
//	 * the first element of the list (the head), 1 the next element and so on.
//	 * 
//	 * For example: LTRIM foobar 0 2 will modify the list stored at foobar so that
//	 * only the first three elements of the list will remain.
//	 * 
//	 * start and end can also be negative numbers indicating offsets from the end of
//	 * the list, where -1 is the last element of the list, -2 the penultimate
//	 * element and so on.
//	 * 
//	 * Out of range indexes will not produce an error: if start is larger than the
//	 * end of the list, or start > end, the result will be an empty list (which
//	 * causes key to be removed). If end is larger than the end of the list, Redis
//	 * will treat it like the last element of the list.
//	 * 
//	 * redis> RPUSH mylist "one"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "two"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "three"<br>
//	 * (integer) 3<br>
//	 * redis> LTRIM mylist 1 -1<br>
//	 * "OK"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "two"<br>
//	 * 2) "three"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * 
//	 * @param key
//	 * @param start
//	 * @param stop
//	 * @return
//	 */
//	String ltrim(final byte[] key, final long start, final long stop);
//
//	byte[] rpop(final byte[] key);
//
//	/**
//	 * <h1>同lpop</h1><br>
//	 * 
//	 * @param key
//	 * @param count
//	 * @return
//	 */
//	List<byte[]> rpop(final byte[] key, final int count);
//
//	/**
//	 * <h1>在一个list上rpop，如果有元素则lpush到另一个list</h1><br>
//	 * 
//	 * Atomically returns and removes the last element (tail) of the list stored at
//	 * source, and pushes the element at the first element (head) of the list stored
//	 * at destination.
//	 * 
//	 * For example: consider source holding the list a,b,c, and destination holding
//	 * the list x,y,z. Executing RPOPLPUSH results in source holding a,b and
//	 * destination holding c,x,y,z.
//	 * 
//	 * If source does not exist, the value nil is returned and no operation is
//	 * performed. If source and destination are the same, the operation is
//	 * equivalent to removing the last element from the list and pushing it as first
//	 * element of the list, so it can be considered as a list rotation command.
//	 * 
//	 * redis> RPUSH mylist "one"<br>
//	 * (integer) 1<br>
//	 * redis> RPUSH mylist "two"<br>
//	 * (integer) 2<br>
//	 * redis> RPUSH mylist "three"<br>
//	 * (integer) 3<br>
//	 * redis> RPOPLPUSH mylist myotherlist<br>
//	 * "three"<br>
//	 * redis> LRANGE mylist 0 -1<br>
//	 * 1) "one"<br>
//	 * 2) "two"<br>
//	 * redis> LRANGE myotherlist 0 -1<br>
//	 * 1) "three"<br>
//	 * redis> <br>
//	 * 
//	 * 
//	 * @param srckey
//	 * @param dstkey
//	 * @return
//	 */
//	byte[] rpoplpush(final byte[] srckey, final byte[] dstkey);
//
//	/**
//	 * <h1>同lpush</h1><br>
//	 * 
//	 * @param key
//	 * @param strings
//	 * @return
//	 */
//	Long rpush(final byte[] key, final byte[]... strings);
//
//	/**
//	 * <h1>同lpushx</h1><br>
//	 * 
//	 * @param key
//	 * @param string
//	 * @return
//	 */
//	Long rpushx(final byte[] key, final byte[]... string);
//}
