package io.github.icodegarden.commons.redis.args;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <K>
 * @param <V>
 */
public class ClaimedMessages<K, V> {

	private final String id;

	private final List<StreamMessage<K, V>> messages;

	/**
	 * Create a new {@link ClaimedMessages}.
	 *
	 * @param id
	 * @param messages
	 */
	public ClaimedMessages(String id, List<StreamMessage<K, V>> messages) {

		this.id = id;
		this.messages = messages;
	}

	public String getId() {
		return id;
	}

	public List<StreamMessage<K, V>> getMessages() {
		return messages;
	}

}
