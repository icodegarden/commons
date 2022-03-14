package io.github.icodegarden.commons.exchange;

import io.github.icodegarden.commons.lang.exception.remote.RemoteException;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface Protocol {

	<R> R exchange(ProtocolParams params) throws RemoteException;

}
