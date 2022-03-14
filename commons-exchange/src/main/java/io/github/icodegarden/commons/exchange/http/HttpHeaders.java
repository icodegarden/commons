package io.github.icodegarden.commons.exchange.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.icodegarden.commons.lang.annotation.Nullable;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public class HttpHeaders implements Map<String, List<String>> {

	private final Map<String, List<String>> targetMap;

	/**
	 * Wrap the given target {@link Map} as a {@link MultiValueMap} adapter.
	 * 
	 * @param targetMap the plain target {@code Map}
	 */
	public HttpHeaders() {
		this.targetMap = new HashMap<String, List<String>>();
	}

	public void setContentLength(long contentLength) {
		set("Content-Length", Long.toString(contentLength));
	}

	public long getContentLength() {
		String value = getFirst("Content-Length");
		return (value != null ? Long.parseLong(value) : -1);
	}

	@Nullable
	public String getFirst(String key) {
		List<String> values = this.targetMap.get(key);
		return (values != null && !values.isEmpty() ? values.get(0) : null);
	}

	public void add(String key, @Nullable String value) {
		List<String> values = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>(1));
		values.add(value);
	}

	public void addAll(String key, List<? extends String> values) {
		List<String> currentValues = this.targetMap.computeIfAbsent(key, k -> new ArrayList<>(1));
		currentValues.addAll(values);
	}

	public void set(String key, @Nullable String value) {
		List<String> values = new ArrayList<>(1);
		values.add(value);
		this.targetMap.put(key, values);
	}

	public void setAll(Map<String, String> values) {
		values.forEach(this::set);
	}

	@Override
	public int size() {
		return this.targetMap.size();
	}

	@Override
	public boolean isEmpty() {
		return this.targetMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return this.targetMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return this.targetMap.containsValue(value);
	}

	@Override
	@Nullable
	public List<String> get(Object key) {
		return this.targetMap.get(key);
	}

	@Override
	@Nullable
	public List<String> put(String key, List<String> value) {
		return this.targetMap.put(key, value);
	}

	@Override
	@Nullable
	public List<String> remove(Object key) {
		return this.targetMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends List<String>> map) {
		this.targetMap.putAll(map);
	}

	@Override
	public void clear() {
		this.targetMap.clear();
	}

	@Override
	public Set<String> keySet() {
		return this.targetMap.keySet();
	}

	@Override
	public Collection<List<String>> values() {
		return this.targetMap.values();
	}

	@Override
	public Set<Entry<String, List<String>>> entrySet() {
		return this.targetMap.entrySet();
	}

	@Override
	public boolean equals(@Nullable Object other) {
		return (this == other || this.targetMap.equals(other));
	}

	@Override
	public int hashCode() {
		return this.targetMap.hashCode();
	}

	@Override
	public String toString() {
		return this.targetMap.toString();
	}
//		private Map<String,String> map = new HashMap<String, String>();
//		void setHeader(String name,String value) {
//			map.put(name, value);
//		}
//		String getHeader(String name) {
//			return map.get(name);
//		}
//		long getContentLength() {
//			String value = map.get("Content-Length");
//			return (value != null ? Long.parseLong(value) : -1);
//		}
//		void setContentLength(long length) {
//			setHeader("Content-Length", Long.toString(length));
//		}
}