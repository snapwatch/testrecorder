package com.almondtools.invivoderived.serializers;

import static java.util.Arrays.asList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.almondtools.invivoderived.SerializedValue;
import com.almondtools.invivoderived.Serializer;
import com.almondtools.invivoderived.SerializerFacade;
import com.almondtools.invivoderived.values.SerializedList;

public class ArrayListSerializer implements Serializer {

	private SerializerFacade facade;

	public ArrayListSerializer(SerializerFacade facade) {
		this.facade = facade;
	}

	@Override
	public List<Class<?>> getMatchingClasses() {
		return asList(List.class, ArrayList.class);
	}

	@Override
	public SerializedValue generate(Type type) {
		return new SerializedList(type);
	}

	@Override
	public void populate(SerializedValue serializedObject, Object object) {
		for (Object element : (List<?>) object) {
			((SerializedList) serializedObject).add(facade.serialize(element.getClass(), element));
		}
	}

}
