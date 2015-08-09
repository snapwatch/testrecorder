package com.almondtools.iit.values;

import java.util.HashMap;
import java.util.Map;

import com.almondtools.iit.SerializedValue;
import com.almondtools.iit.SerializedValueVisitor;
import com.almondtools.iit.visitors.SerializedValuePrinter;

public class SerializedNull implements SerializedValue {

	private static final Map<Class<?>, SerializedNull> KNOWN_LITERALS = new HashMap<>();
	
	private Class<?> type;
	
	public SerializedNull(Class<?> type) {
		this.type = type;
	}
	
	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public <T> T accept(SerializedValueVisitor<T> visitor) {
		return visitor.visitNull(this);
	}

	public static SerializedNull of(Class<?> type) {
		return KNOWN_LITERALS.computeIfAbsent(type, typ -> new SerializedNull(typ));
	}

	@Override
	public String toString() {
		return accept(new SerializedValuePrinter());
	}

	@Override
	public int hashCode() {
		return type.getName().hashCode() * 7 + 29;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SerializedNull that = (SerializedNull) obj;
		return this.type == that.type;
	}
	
}
