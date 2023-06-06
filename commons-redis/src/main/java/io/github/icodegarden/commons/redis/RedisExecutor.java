package io.github.icodegarden.commons.redis;

import java.io.IOException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface RedisExecutor extends KeyBinaryCommands, StringBinaryCommands, HashBinaryCommands, ListBinaryCommands,
		SetBinaryCommands, SortedSetBinaryCommands, BitmapBinaryCommands, GeoBinaryCommands, HyperLogLogBinaryCommands,
		ScriptingBinaryCommands, PubSubBinaryCommands {

	void close() throws IOException;

}
