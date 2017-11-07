package net.amygdalum.testrecorder.deserializers;

import static net.amygdalum.testrecorder.deserializers.DeserializerContext.NULL;
import static net.amygdalum.testrecorder.values.SerializedLiteral.literal;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import net.amygdalum.testrecorder.Deserializer;
import net.amygdalum.testrecorder.SerializedImmutableType;
import net.amygdalum.testrecorder.SerializedReferenceType;
import net.amygdalum.testrecorder.SerializedValueType;
import net.amygdalum.testrecorder.util.testobjects.Simple;
import net.amygdalum.testrecorder.values.SerializedField;
import net.amygdalum.testrecorder.values.SerializedImmutable;
import net.amygdalum.testrecorder.values.SerializedLiteral;
import net.amygdalum.testrecorder.values.SerializedObject;

public class MappedDeserializerTest {

	private Function<Integer, Long> mapping;
	private Deserializer<Integer> deserializer;
	private MappedDeserializer<Long, Integer> mappedDeserializer;

	@SuppressWarnings("unchecked")
	@Before
	public void before() throws Exception {
		deserializer = mock(Deserializer.class);
		mapping = x -> (long) x;
		mappedDeserializer = new MappedDeserializer<>(deserializer, mapping);
	}

	@Test
	public void testVisitField() throws Exception {
		SerializedField field = new SerializedField(Simple.class, "str", String.class, literal("v"));
		when(deserializer.visitField(field, NULL)).thenReturn(2);

		assertThat(mappedDeserializer.visitField(field, NULL), equalTo(2l));
	}

	@Test
	public void testVisitReferenceType() throws Exception {
		SerializedReferenceType object = new SerializedObject(Simple.class);
		when(deserializer.visitReferenceType(object, NULL)).thenReturn(3);

		assertThat(mappedDeserializer.visitReferenceType(object, NULL), equalTo(3l));
	}

	@Test
	public void testVisitImmutableType() throws Exception {
		SerializedImmutableType object = new SerializedImmutable<>(BigInteger.class);
		when(deserializer.visitImmutableType(object, NULL)).thenReturn(4);

		assertThat(mappedDeserializer.visitImmutableType(object, NULL), equalTo(4l));
	}

	@Test
	public void testVisitValueType() throws Exception {
		SerializedValueType object = SerializedLiteral.literal("lit");
		when(deserializer.visitValueType(object, NULL)).thenReturn(5);

		assertThat(mappedDeserializer.visitValueType(object, NULL), equalTo(5l));
	}

}
