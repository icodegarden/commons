package io.github.icodegarden.commons.lang;

import java.util.List;

import io.github.icodegarden.commons.lang.util.ClassUtils;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface IdObject<ID> {

	ID getId();

	void setId(ID id);

	@SuppressWarnings("all")
	public static void setIdIfNecessary(Object id, Object target) {
		if (target instanceof IdObject && ((IdObject) target).getId() == null) {
			List<Class<?>> types = ClassUtils.getGenericInterfaceTypes(target, IdObject.class);
			if (!types.isEmpty() && types.get(0).equals(id.getClass())) {
				/**
				 * 要求参数类型相同
				 */
				((IdObject) target).setId(id);
			}
		}
	}
}
