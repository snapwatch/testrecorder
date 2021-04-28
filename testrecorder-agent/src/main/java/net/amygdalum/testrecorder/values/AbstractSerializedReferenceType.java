package net.amygdalum.testrecorder.values;

import static java.util.Comparator.comparing;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import net.amygdalum.testrecorder.types.SerializedReferenceType;

public abstract class AbstractSerializedReferenceType extends AbstractSerializedValue implements SerializedReferenceType {

    private int id;
    private Type[] usedTypes;

    public AbstractSerializedReferenceType(Class<?> type) {
        super(type);
    }
    
    @Override
    public int getId() {
		return id;
	}
    
    @Override
    public void setId(int id) {
		this.id = id;
	}

    @Override
    public Type[] getUsedTypes() {
        if (usedTypes == null) {
            return super.getUsedTypes();
        } else {
            return usedTypes;
        }
    }

    @Override
    public void useAs(Type type) {
    	assert type instanceof Serializable;
    	if (usedTypes == null) {
    		usedTypes = new Type[] {type};
    		return;
    	}
    	Type[] types = usedTypes;
		int pos = Arrays.binarySearch(types, type, comparing(Type::getTypeName));
		if (pos < 0) {
			usedTypes = new Type[types.length + 1];
			int index = -pos - 1;
			System.arraycopy(types, 0, usedTypes, 0, index);
			usedTypes[index] = type;
			System.arraycopy(types, index, usedTypes, index+1, types.length - index);
		}
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AbstractSerializedReferenceType that = (AbstractSerializedReferenceType) o;
        boolean idEqual = id == that.id;
        return idEqual &&
                Arrays.equals(usedTypes, that.usedTypes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(super.hashCode(), id);
        result = 31 * result + Arrays.hashCode(usedTypes);
        return result;
    }
}
