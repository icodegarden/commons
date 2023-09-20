package io.github.icodegarden.commons.lang.concurrent.registry;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <R>
 */
public interface Registry<R extends Registration> extends Discovery<Registration> {

	void register(R registration) throws RegistryException;

	void deregister(R registration) throws RegistryException;

	void close();

//	void setStatus(R registration, String status);
//
//	<T> T getStatus(R registration);

}
