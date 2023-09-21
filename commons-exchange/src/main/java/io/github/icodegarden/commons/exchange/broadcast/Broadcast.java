package io.github.icodegarden.commons.exchange.broadcast;

import java.io.IOException;

import io.github.icodegarden.commons.exchange.ParallelExchangeResult;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Broadcast {

//	/**
//	 * 单向广播发送，没有response，发送成功就视为成功
//	 */
//	ParallelExchangeResult send(BroadcastMessage message);

	/**
	 * 广播发送并且有response
	 */
	ParallelExchangeResult request(BroadcastMessage message);

	void close() throws IOException;
}
