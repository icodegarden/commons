package io.github.icodegarden.commons.lang.concurrent.registry;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <R>
 */
public interface Discovery<R extends Registration> {

	List<R> listInstances(String name);
}
