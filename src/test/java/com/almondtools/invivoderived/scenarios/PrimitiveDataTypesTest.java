package com.almondtools.invivoderived.scenarios;

import static com.almondtools.invivoderived.analyzer.SnapshotGenerator.setSnapshotConsumer;
import static com.almondtools.invivoderived.dynamiccompile.CompilableMatcher.compiles;
import static com.almondtools.invivoderived.dynamiccompile.TestsRunnableMatcher.testsRuns;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.almondtools.invivoderived.analyzer.SnapshotInstrumentor;
import com.almondtools.invivoderived.generator.TestGenerator;

public class PrimitiveDataTypesTest {

	private static SnapshotInstrumentor instrumentor;

	private TestGenerator testGenerator;

	@BeforeClass
	public static void beforeClass() throws Exception {
		instrumentor = new SnapshotInstrumentor();
		instrumentor.register("com.almondtools.invivoderived.scenarios.PrimitiveDataTypes");
	}
	
	@Before
	public void before() throws Exception {
		testGenerator = new TestGenerator();
		setSnapshotConsumer(testGenerator);
	}

	@Test
	public void testCompilable() throws Exception {
		PrimitiveDataTypes dataTypes = new PrimitiveDataTypes();
		for (int i = 1; i <= 10; i++) {
			dataTypes.booleans(i % 2 == 0);
			dataTypes.chars((char) i);
			dataTypes.bytes((byte) i);
			dataTypes.shorts((short) i);
			dataTypes.integers(i);
			dataTypes.floats((float) i);
			dataTypes.longs(i);
			dataTypes.doubles((double) i);
		}
		assertThat(testGenerator.renderTest(PrimitiveDataTypes.class), compiles());
		assertThat(testGenerator.renderTest(PrimitiveDataTypes.class), testsRuns());
	}
}