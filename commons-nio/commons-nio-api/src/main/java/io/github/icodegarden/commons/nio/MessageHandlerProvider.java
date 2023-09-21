package io.github.icodegarden.commons.nio;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface MessageHandlerProvider<T, R> {

	 MessageHandler<T, R> getMessageHandler();

	boolean supports(Object msg);

}
