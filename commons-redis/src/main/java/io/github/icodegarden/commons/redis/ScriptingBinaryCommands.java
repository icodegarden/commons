package io.github.icodegarden.commons.redis;

import java.util.List;

import io.github.icodegarden.commons.redis.args.FlushMode;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ScriptingBinaryCommands {

	/**
	 * EVAL script numkeys [key [key ...]] [arg [arg ...]]
	 * 
	 * @param script
	 * @return
	 */
	List<Object> eval(byte[] script);

	/**
	 * 
	 * EVAL script numkeys [key [key ...]] [arg [arg ...]]
	 * 
	 * @param script
	 * @param keyCount
	 * @param params   约定形式是 key1,key1对应的参数,key2,key2对应的参数,...
	 * @return
	 */
	List<Object> eval(byte[] script, int keyCount, byte[]... params);

	/**
	 * EVAL script numkeys [key [key ...]] [arg [arg ...]]
	 * 
	 * Invoke the execution of a server-side Lua script.
	 * 
	 * The first argument is the script's source code. Scripts are written in Lua
	 * and executed by the embedded Lua 5.1 interpreter in Redis.
	 * 
	 * The second argument is the number of input key name arguments, followed by
	 * all the keys accessed by the script. These names of input keys are available
	 * to the script as the KEYS global runtime variable Any additional input
	 * arguments should not represent names of keys.
	 * 
	 * Important: to ensure the correct execution of scripts, both in standalone and
	 * clustered deployments, all names of keys that a script accesses must be
	 * explicitly provided as input key arguments. The script should only access
	 * keys whose names are given as input arguments. Scripts should never access
	 * keys with programmatically-generated names or based on the contents of data
	 * structures stored in the database.
	 * 
	 * Please refer to the Redis Programmability and Introduction to Eval Scripts
	 * for more information about Lua scripts.
	 * 
	 * > EVAL "return ARGV[1]" 0 hello<br>
	 * "hello"<br>
	 * 
	 * @param script
	 * @param keys
	 * @param args
	 * @return
	 */
	List<Object> eval(byte[] script, List<byte[]> keys, List<byte[]> args);

	List<Object> evalReadonly(byte[] script, List<byte[]> keys, List<byte[]> args);

	List<Object> evalsha(String sha1);

	List<Object> evalsha(String sha1, int keyCount, byte[]... params);

	List<Object> evalsha(String sha1, List<byte[]> keys, List<byte[]> args);

	List<Object> evalshaReadonly(String sha1, List<byte[]> keys, List<byte[]> args);

	List<Boolean> scriptExists(String... sha1s);

	String scriptFlush(FlushMode flushMode);

	String scriptKill();

	String scriptLoad(byte[] script);
}
