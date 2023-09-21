package io.github.icodegarden.commons.lang;

/**
 * 
 * @author Fangfang.Xu
 *
 * @param <T>
 */
public interface Matcher<T> {

	boolean matches(T object);
}