package io.github.icodegarden.commons.nio;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MessageHandler {

	/**
	 * for send only without response 
	 */
	void receive(Object obj);

	Object reply(Object obj);
}
