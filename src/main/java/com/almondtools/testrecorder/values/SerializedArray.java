package com.almondtools.testrecorder.values;

import static java.util.Arrays.asList;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.almondtools.testrecorder.SerializedValue;
import com.almondtools.testrecorder.SerializedValueVisitor;
import com.almondtools.testrecorder.TypeHelper;
import com.almondtools.testrecorder.visitors.SerializedValuePrinter;

public class SerializedArray implements SerializedValue {

	private Type type;
	private List<SerializedValue> array;

	public SerializedArray(Type type) {
		this.type = type;
		this.array = new ArrayList<>();
	}

	public SerializedArray with(SerializedValue... values) {
		array.addAll(asList(values));
		return this;
	}
	
	@Override
	public Type getType() {
		return type;
	}
	
	@Override
	public Class<?> getValueType() {
		return TypeHelper.getBase(type);
	}
	
	public Type getComponentType() {
		if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
			return ((Class<?>) type).getComponentType();
		} else if (type instanceof GenericArrayType) {
			return ((GenericArrayType) type).getGenericComponentType();
		} else {
			return Object.class;
		}
	}

	public Class<?> getRawType() {
		if (type instanceof Class<?> && ((Class<?>) type).isArray()) {
			return ((Class<?>) type).getComponentType();
		} else {
			return Object.class;
		}
	}

	public SerializedValue[] getArray() {
		return array.toArray(new SerializedValue[0]);
	}

	public List<SerializedValue> getArrayAsList() {
		return array;
	}
	
	@Override
	public <T> T accept(SerializedValueVisitor<T> visitor) {
		return visitor.visitArray(this);
	}
	
	public void add(SerializedValue value) {
		array.add(value);
	}

	@Override
	public String toString() {
		return accept(new SerializedValuePrinter());
	}

}
