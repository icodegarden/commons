package io.github.icodegarden.commons.nio;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MessageHandler {

	void receive(Object obj);

	Object reply(Object obj);
}
