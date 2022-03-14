//package io.github.icodegarden.commons.lang.util;
//
//import java.lang.reflect.Field;
//import java.util.Arrays;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * 
// * @author Fangfang.Xu
// *
// */
//public class BeanUtils {
//
//	/**
//	 * copy本类和父类递归的字段 FIXME 有final字段赋值问题
//	 * @param source
//	 * @param target
//	 */
//	public static void copyProperties(Object source, Object target) {
//		copyProperties(source, source.getClass(), target, target.getClass());
//	}
//
//	private static void copyProperties(Object source, Class<?> sourceClass, Object target, Class<?> targetClass) {
//		if (sourceClass == Object.class || targetClass == Object.class) {
//			return;
//		}
//
//		Field[] sourceFields = sourceClass.getDeclaredFields();
//		Field[] targetFields = targetClass.getDeclaredFields();
//
//		Map<String, Field> targetFieldsMap = Arrays.asList(targetFields).stream()
//				.collect(Collectors.toMap(Field::getName, f -> f));
//
//		for (Field sourceField : sourceFields) {
//			Field targetField = targetFieldsMap.get(sourceField.getName());
//			if (targetField != null) {
//				boolean sourceFieldAccessible = sourceField.isAccessible();
//				boolean targetFieldAccessible = targetField.isAccessible();
//				if (!sourceFieldAccessible) {
//					sourceField.setAccessible(true);
//				}
//				if (!targetFieldAccessible) {
//					targetField.setAccessible(true);
//				}
//				try {
//					targetField.set(target, sourceField.get(source));
//				} catch (IllegalAccessException e) {
//					throw new IllegalStateException(e);
//				}
//
//				if (!sourceFieldAccessible) {
//					sourceField.setAccessible(sourceFieldAccessible);
//				}
//				if (!targetFieldAccessible) {
//					targetField.setAccessible(targetFieldAccessible);
//				}
//			}
//		}
//
//		copyProperties(source, sourceClass.getSuperclass(), target, targetClass.getSuperclass());
//	}
//}
