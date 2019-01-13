package net.amygdalum.testrecorder.data;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RandomByteValueGeneratorTest {

	private RandomByteValueGenerator gen;

	@BeforeEach
	public void before() throws Exception {
		gen = new RandomByteValueGenerator();
	}

	@Test
	void testCreateMax() throws Exception {
		gen.random.setSeed(Long.MAX_VALUE);

		assertThat(gen.create(null)).isEqualTo((byte) -77);
	}

	@Test
	void testCreateMin() throws Exception {
		gen.random.setSeed(Long.MIN_VALUE);

		assertThat(gen.create(null)).isEqualTo((byte) 96);
	}

}
