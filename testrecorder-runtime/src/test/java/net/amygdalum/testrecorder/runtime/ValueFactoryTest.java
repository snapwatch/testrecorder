package net.amygdalum.testrecorder.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ValueFactoryTest {

    @Test
    void testNewValue() throws Exception {
        assertThat(ValueFactory.NONE.newValue(Object.class)).isNull();
    }

    @Test
    void testGetDescription() throws Exception {
        assertThat(ValueFactory.NONE.getDescription(Object.class)).isEqualTo("null");
        assertThat(new ValueFactory() {

            @Override
            public Object newValue(Class<?> clazz) {
                return new Object() {
                    @Override
                    public String toString() {
                        throw new RuntimeException();
                    }
                };
            }
        }.getDescription(Object.class)).isEqualTo("<undescribable>");
    }
}
