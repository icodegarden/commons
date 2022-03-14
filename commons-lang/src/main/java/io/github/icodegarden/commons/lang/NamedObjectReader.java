package io.github.icodegarden.commons.lang;

import java.util.List;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface NamedObjectReader<T extends NamedObject> {

	List<T> listNamedObjects(String name);

}
