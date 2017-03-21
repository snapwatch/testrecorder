package net.amygdalum.testrecorder.util;

import static net.amygdalum.testrecorder.util.Reflections.accessing;
import static net.amygdalum.testrecorder.util.Types.allFields;
import static net.amygdalum.testrecorder.values.SerializedLiteral.isLiteral;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.List;

public class GenericComparison {
	private static final GenericComparison NULL = new GenericComparison("<error>", null, null);
	
	private String root;
	private Object left;
	private Object right;
	private Boolean mismatch;
    private List<String> fields;

	public GenericComparison(String root, Object left, Object right) {
		this.root = root;
		this.left = left;
		this.right = right;
		this.mismatch = null;
		this.fields = null;
	}

    public GenericComparison(String root, Object left, Object right, List<String> fields) {
        this.root = root;
        this.left = left;
        this.right = right;
        this.mismatch = null;
        this.fields = fields;
    }

	public String getRoot() {
		return root;
	}

	public Object getLeft() {
		return left;
	}

	public Object getRight() {
		return right;
	}

	public void setMismatch(Boolean mismatch) {
		this.mismatch = mismatch;
	}

	public static boolean equals(String root, Object o1, Object o2) {
		return equals(new GenericComparison(root, o1, o2));
	}

    public static boolean equals(String root, Object o1, Object o2, List<String> fields) {
        return equals(new GenericComparison(root, o1, o2, fields));
    }

	public static boolean equals(GenericComparison p) {
		WorkSet<GenericComparison> todo = new WorkSet<>();
		todo.add(p);
		return equals(todo);
	}

	public static boolean equals(WorkSet<GenericComparison> todo) {
		while (todo.hasMoreElements()) {
			GenericComparison current = todo.remove();
			if (!current.eval(todo)) {
				return false;
			}
		}
		return true;
	}

	public boolean eval(WorkSet<GenericComparison> todo) {
		if (left == right) {
			return true;
		} else if (left == null || right == null) {
			return false;
		} else if (left.getClass() != right.getClass()) {
			return false;
		}
		Class<?> clazz = left.getClass();
		if (isLiteral(clazz)) {
			return left.equals(right);
		}
		if (clazz.isArray()) {
		    int length = Array.getLength(left);
            if (length != Array.getLength(right)) {
		        return false;
		    }
            for (int i = 0; i < length; i++) {
                todo.add(new GenericComparison(root, Array.get(left, i), Array.get(right, i)));
            }
		}
		for (Field field : allFields(clazz)) {
			if (field.isSynthetic()) {
				continue;
			}
			String fieldName = field.getName();
            if (fields != null && !fields.contains(fieldName)) {
                continue;
            }
			todo.add(GenericComparison.from(root, fieldName, left, right));
		}
		return true;
	}

	public boolean eval(GenericComparator comparator, WorkSet<GenericComparison> todo) {
		if (left == right) {
			return true;
		}
		GenericComparatorResult compare = comparator.compare(this, todo);
		if (compare.isApplying()) {
			return compare.getResult();
		}
		if (left == null || right == null) {
			return false;
		}
		Class<?> clazz = left.getClass();
		if (isLiteral(clazz)) {
			return left.equals(right);
		}
        if (clazz.isArray()) {
            int length = Array.getLength(left);
            if (length != Array.getLength(right)) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                todo.add(new GenericComparison(root, Array.get(left, i), Array.get(right, i)));
            }
        }
		for (Field field : allFields(clazz)) {
			if (field.isSynthetic()) {
				continue;
			}
			String fieldName = field.getName();
            if (fields != null && !fields.contains(fieldName)) {
                continue;
            }
			todo.add(GenericComparison.from(root, fieldName, left, right));
		}
		return true;
	}

	public static Object getValue(String fieldName, Object item) throws ReflectiveOperationException {
		Field field = Types.getDeclaredField(item.getClass(), fieldName);

		return getValue(field, item);
	}

	public static Object getValue(Field field, Object item) throws ReflectiveOperationException {
		return accessing(field).call(() -> field.get(item));
	}

	public static GenericComparison from(String root, String field, Object left, Object right) {
		try {
			Object f1 = getValue(field, left);
			Object f2 = getValue(field, right);
			String newRoot = root == null ? field : root + '.' + field;
			return new GenericComparison(newRoot, f1, f2);
		} catch (ReflectiveOperationException e) {
			return GenericComparison.NULL;
		}
	}

	public static void compare(WorkSet<GenericComparison> remainder, GenericComparator comparator) {
		while (remainder.hasMoreElements()) {
			GenericComparison current = remainder.remove();
			if (!current.eval(comparator, remainder)) {
				current.setMismatch(true);
			}
		}
	}

	public boolean isMismatch() {
		return mismatch == null ? false : mismatch;
	}

	@Override
	public int hashCode() {
		return 17 + System.identityHashCode(left) * 13 + System.identityHashCode(right) * 7;
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
		GenericComparison that = (GenericComparison) obj;
		return this.right == that.right
			&& this.left == that.left;
	}

}