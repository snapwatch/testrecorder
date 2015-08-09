package com.almondtools.iit.values;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.almondtools.iit.SerializedValue;
import com.almondtools.iit.SerializedValueVisitor;
import com.almondtools.iit.visitors.SerializedValuePrinter;

public class SerializedMap implements SerializedValue, Map<SerializedValue, SerializedValue> {

	private Map<SerializedValue, SerializedValue> map;

	public SerializedMap() {
		map = new LinkedHashMap<>();
	}
	
	@Override
	public Class<?> getType() {
		return Map.class;
	}

	@Override
	public <T> T accept(SerializedValueVisitor<T> visitor) {
		return visitor.visitMap(this);
	}
	
	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public SerializedValue get(Object key) {
		return map.get(key);
	}

	public SerializedValue put(SerializedValue key, SerializedValue value) {
		return map.put(key, value);
	}

	public SerializedValue remove(Object key) {
		return map.remove(key);
	}

	public void putAll(Map<? extends SerializedValue, ? extends SerializedValue> m) {
		map.putAll(m);
	}

	public void clear() {
		map.clear();
	}

	public Set<SerializedValue> keySet() {
		return map.keySet();
	}

	public Collection<SerializedValue> values() {
		return map.values();
	}

	public Set<java.util.Map.Entry<SerializedValue, SerializedValue>> entrySet() {
		return map.entrySet();
	}

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public String toString() {
		return accept(new SerializedValuePrinter());
	}
}
