package net.amygdalum.testrecorder.deserializers.builder;

import static net.amygdalum.testrecorder.deserializers.DefaultDeserializerContext.NULL;
import static net.amygdalum.testrecorder.util.testobjects.Hidden.classOfHiddenEnum;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.amygdalum.testrecorder.deserializers.Computation;
import net.amygdalum.testrecorder.util.testobjects.PublicEnum;
import net.amygdalum.testrecorder.values.SerializedEnum;

public class DefaultEnumAdaptorTest {

	private DefaultEnumAdaptor adaptor;

	@BeforeEach
	public void before() throws Exception {
		adaptor = new DefaultEnumAdaptor();
	}

	@Test
	public void testParentNull() throws Exception {
		assertThat(adaptor.parent(), nullValue());
	}

	@Test
	public void testMatchesOnlyEnum() throws Exception {
		assertThat(adaptor.matches(PublicEnum.class)).isTrue();
		assertThat(adaptor.matches(classOfHiddenEnum())).isTrue();
		assertThat(adaptor.matches(Enum.class)).isFalse();
		assertThat(adaptor.matches(Object.class)).isFalse();
	}

	@Test
	public void testTryDeserialize() throws Exception {
		SerializedEnum value = new SerializedEnum(PublicEnum.class);
		value.setName("VALUE1");
		SetupGenerators generator = new SetupGenerators(getClass());

		Computation result = adaptor.tryDeserialize(value, generator, NULL);

		assertThat(result.getStatements(), empty());
		assertThat(result.getValue()).isEqualTo("PublicEnum.VALUE1");
	}

	@Test
	public void testTryDeserializeHidden() throws Exception {
		SerializedEnum value = new SerializedEnum(classOfHiddenEnum());
		value.setName("VALUE2");
		SetupGenerators generator = new SetupGenerators(getClass());
		
		Computation result = adaptor.tryDeserialize(value, generator, NULL);
		
		assertThat(result.getStatements(), empty());
		assertThat(result.getValue(), containsString("Wrapped.enumType(\"net.amygdalum.testrecorder.util.testobjects.Hidden$HiddenEnum\", \"VALUE2\").value()"));
	}
	
}
