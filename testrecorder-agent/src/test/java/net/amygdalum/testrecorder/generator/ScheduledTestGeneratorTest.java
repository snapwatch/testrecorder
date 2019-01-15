package net.amygdalum.testrecorder.generator;

import static net.amygdalum.extensions.assertj.Assertions.assertThat;
import static net.amygdalum.testrecorder.TestAgentConfiguration.defaultConfig;
import static net.amygdalum.testrecorder.values.SerializedLiteral.literal;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.amygdalum.testrecorder.SnapshotManager;
import net.amygdalum.testrecorder.TestAgentConfiguration;
import net.amygdalum.testrecorder.types.ContextSnapshot;
import net.amygdalum.testrecorder.types.FieldSignature;
import net.amygdalum.testrecorder.types.MethodSignature;
import net.amygdalum.testrecorder.types.SerializedField;
import net.amygdalum.testrecorder.types.SerializedInput;
import net.amygdalum.testrecorder.types.SerializedOutput;
import net.amygdalum.testrecorder.types.VirtualMethodSignature;
import net.amygdalum.testrecorder.util.ClassDescriptor;
import net.amygdalum.testrecorder.util.ExtensibleClassLoader;
import net.amygdalum.testrecorder.util.TemporaryFolder;
import net.amygdalum.testrecorder.util.TemporaryFolderExtension;
import net.amygdalum.testrecorder.values.SerializedObject;
import net.amygdalum.xrayinterface.XRayInterface;

@ExtendWith(TemporaryFolderExtension.class)
public class ScheduledTestGeneratorTest {

	private static SnapshotManager saveManager;

	private ExtensibleClassLoader loader;
	private TestAgentConfiguration config;
	private ScheduledTestGenerator testGenerator;

	@BeforeAll
	static void beforeClass() throws Exception {
		saveManager = SnapshotManager.MANAGER;
	}

	@AfterAll
	static void afterClass() throws Exception {
		SnapshotManager.MANAGER = saveManager;
		shutdownHooks().entrySet().stream()
			.filter(e -> e.getKey().getName().equals("$generate-shutdown"))
			.map(e -> e.getValue())
			.findFirst()
			.ifPresent(shutdown -> {
				Runtime.getRuntime().removeShutdownHook(shutdown);
				XRayInterface.xray(ScheduledTestGenerator.class).to(OpenScheduledTestGenerator.class).setDumpOnShutDown(null);
			});
	}

	@BeforeEach
	void before() throws Exception {
		XRayInterface.xray(ScheduledTestGenerator.class).to(OpenScheduledTestGenerator.class).setDumpOnShutDown(null);
		loader = new ExtensibleClassLoader(ScheduledTestGenerator.class.getClassLoader());
		config = defaultConfig().withLoader(loader);
		testGenerator = new ScheduledTestGenerator(config);
		testGenerator.counterMaximum = 1;
	}

	@Nested
	class testAccept {
		@Test
		void onCommon() throws Exception {
			ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
			FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
			snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
			snapshot.setSetupArgs(literal(int.class, 16));
			snapshot.setSetupGlobals(new SerializedField[0]);
			snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
			snapshot.setExpectArgs(literal(int.class, 16));
			snapshot.setExpectResult(literal(int.class, 22));
			snapshot.setExpectGlobals(new SerializedField[0]);

			testGenerator.accept(snapshot);

			testGenerator.await();
			assertThat(testGenerator.testsFor(ScheduledTestGeneratorTest.class))
				.hasSize(1)
				.anySatisfy(test -> {
					assertThat(test)
						.containsSubsequence(
							"int field = 12;",
							"intMethod(16);",
							"equalTo(22)",
							"int field = 8;");
				});
		}

		@Test
		void withInput() throws Exception {
			ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
			FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
			snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
			snapshot.setSetupArgs(literal(int.class, 16));
			snapshot.setSetupGlobals(new SerializedField[0]);
			snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
			snapshot.setExpectArgs(literal(int.class, 16));
			snapshot.setExpectResult(literal(int.class, 22));
			snapshot.setExpectGlobals(new SerializedField[0]);
			snapshot.addInput(new SerializedInput(42, new MethodSignature(System.class, long.class, "currentTimeMillis", new Type[0])).updateResult(literal(42l)));

			testGenerator.accept(snapshot);

			testGenerator.await();
			assertThat(testGenerator.renderTest(ScheduledTestGeneratorTest.class).getTestCode())
				.containsSubsequence(
					"@Before",
					"@After",
					"public void resetFakeIO() throws Exception {",
					"FakeIO.reset();",
					"}");
		}

		@Test
		void withOutput() throws Exception {
			ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
			FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
			snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
			snapshot.setSetupArgs(literal(int.class, 16));
			snapshot.setSetupGlobals(new SerializedField[0]);
			snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
			snapshot.setExpectArgs(literal(int.class, 16));
			snapshot.setExpectResult(literal(int.class, 22));
			snapshot.setExpectGlobals(new SerializedField[0]);
			snapshot.addOutput(new SerializedOutput(42, new MethodSignature(Writer.class, void.class, "write", new Type[] {Writer.class})).updateArguments(literal("hello")));

			testGenerator.accept(snapshot);

			testGenerator.await();
			assertThat(testGenerator.renderTest(ScheduledTestGeneratorTest.class).getTestCode())
				.containsSubsequence(
					"@Before",
					"@After",
					"public void resetFakeIO() throws Exception {",
					"FakeIO.reset();",
					"}");
		}

		@Test
		void suppressinWarnings() throws Exception {
			ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
			FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
			snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
			snapshot.setSetupArgs(literal(int.class, 16));
			snapshot.setSetupGlobals(new SerializedField[0]);
			snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
			snapshot.setExpectArgs(literal(int.class, 16));
			snapshot.setExpectResult(literal(int.class, 22));
			snapshot.setExpectGlobals(new SerializedField[0]);

			testGenerator.accept(snapshot);

			testGenerator.await();
			assertThat(testGenerator.renderTest(MyClass.class).getTestCode()).containsSubsequence("@SuppressWarnings(\"unused\")" + System.lineSeparator() + "public class");
		}
	}

	@Nested
	class testTestsFor {
		@Test
		void onEmpty() throws Exception {
			assertThat(testGenerator.testsFor(MyClass.class)).isEmpty();
		}

		@Test
		void afterClear() throws Exception {
			ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
			FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
			snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
			snapshot.setSetupArgs(literal(int.class, 16));
			snapshot.setSetupGlobals(new SerializedField[0]);
			snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
			snapshot.setExpectArgs(literal(int.class, 16));
			snapshot.setExpectResult(literal(int.class, 22));
			snapshot.setExpectGlobals(new SerializedField[0]);
			testGenerator.accept(snapshot);

			testGenerator.clearResults();

			assertThat(testGenerator.testsFor(MyClass.class)).isEmpty();
		}
	}

	@Test
	void testRenderTest() throws Exception {
		FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");

		ContextSnapshot snapshot1 = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
		snapshot1.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
		snapshot1.setSetupArgs(literal(int.class, 16));
		snapshot1.setSetupGlobals(new SerializedField[0]);
		snapshot1.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
		snapshot1.setExpectArgs(literal(int.class, 16));
		snapshot1.setExpectResult(literal(int.class, 22));
		snapshot1.setExpectGlobals(new SerializedField[0]);
		ContextSnapshot snapshot2 = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
		snapshot2.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 13))));
		snapshot2.setSetupArgs(literal(int.class, 17));
		snapshot2.setSetupGlobals(new SerializedField[0]);
		snapshot2.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 9))));
		snapshot2.setExpectArgs(literal(int.class, 17));
		snapshot2.setExpectResult(literal(int.class, 23));
		snapshot2.setExpectGlobals(new SerializedField[0]);

		testGenerator.counterMaximum = 2;
		testGenerator.accept(snapshot1);
		testGenerator.accept(snapshot2);

		testGenerator.await();
		assertThat(testGenerator.renderTest(ScheduledTestGeneratorTest.class).getTestCode()).containsSubsequence(
			"int field = 12;",
			"intMethod(16);",
			"equalTo(22)",
			"int field = 8;",
			"int field = 13;",
			"intMethod(17);",
			"equalTo(23)",
			"int field = 9;");
	}

	@Nested
	class testComputeClassName {
		@Test
		void onCommon() throws Exception {
			assertThat(testGenerator.computeClassName(ClassDescriptor.of(MyClass.class))).isEqualTo("MyClassRecordedTest");
		}

		@Test
		void withTemplateClass() throws Exception {
			testGenerator.classNameTemplate = "${class}Suffix";
			assertThat(testGenerator.computeClassName(ClassDescriptor.of(MyClass.class))).isEqualTo("MyClassSuffix");
		}

		@Test
		void withTemplateCounter() throws Exception {
			testGenerator.classNameTemplate = "${counter}Suffix";
			assertThat(testGenerator.computeClassName(ClassDescriptor.of(MyClass.class))).isEqualTo("0Suffix");
		}

		@Test
		void withTemplateMillis() throws Exception {
			testGenerator.classNameTemplate = "Prefix${millis}Suffix";
			assertThat(testGenerator.computeClassName(ClassDescriptor.of(MyClass.class))).containsWildcardPattern("Prefix*Suffix");
		}
	}

	@Test
	void testWriteResults(TemporaryFolder folder) throws Exception {
		ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
		FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
		snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 12))));
		snapshot.setSetupArgs(literal(int.class, 16));
		snapshot.setSetupGlobals(new SerializedField[0]);
		snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, 8))));
		snapshot.setExpectArgs(literal(int.class, 16));
		snapshot.setExpectResult(literal(int.class, 22));
		snapshot.setExpectGlobals(new SerializedField[0]);

		testGenerator.accept(snapshot);

		testGenerator.await();
		testGenerator.writeResults(folder.getRoot());

		assertThat(Files.exists(folder.resolve("net/amygdalum/testrecorder/generator/ScheduledTestGeneratorTestRecordedTest.java"))).isTrue();
	}

	@Test
	void testWithDumpOnTimeInterval(TemporaryFolder folder) throws Exception {
		testGenerator.counterMaximum = 5;
		testGenerator.classNameTemplate = "${counter}Test";
		testGenerator.timeInterval = 1000;
		testGenerator.generateTo = folder.getRoot();

		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).isEmpty();
		Thread.sleep(1000);
		testGenerator.accept(newSnapshot());
		testGenerator.await();

		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java");

		testGenerator.accept(newSnapshot());
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java");
		Thread.sleep(1000);
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java", "2Test.java");
	}

	@Test
	void testWithDumpOnCounterInterval(TemporaryFolder folder) throws Exception {
		testGenerator.counterMaximum = 5;
		testGenerator.generateTo = folder.getRoot();
		testGenerator.classNameTemplate = "${counter}Test";
		testGenerator.counterInterval = 2;

		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).isEmpty();
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java");
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java");
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java", "2Test.java");
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java", "2Test.java");
		testGenerator.accept(newSnapshot());
		testGenerator.await();
		assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java", "2Test.java");
	}

	@Nested
	class testWithDumpOnShutDown {
		@Test
		void singleThread(TemporaryFolder folder) throws Exception {
			testGenerator.counterMaximum = 5;
			testGenerator.generateTo = folder.getRoot();
			testGenerator.classNameTemplate = "${counter}Test";
			testGenerator.dumpOnShutdown(true);

			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.await();
			assertThat(folder.fileNames()).isEmpty();

			Thread shutdown = shutdownHooks().entrySet().stream()
				.filter(e -> e.getKey().getName().equals("$generate-shutdown"))
				.map(e -> e.getValue())
				.findFirst().orElseThrow(() -> new AssertionError("no shutdown thread"));

			shutdown.run();
			shutdown.join();

			assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java");
		}

		@Test
		void concurrent(TemporaryFolder folder) throws Exception {
			testGenerator.counterMaximum = 2;
			testGenerator.generateTo = folder.getRoot();
			testGenerator.classNameTemplate = "${counter}Test";
			testGenerator.dumpOnShutdown(true);

			ScheduledTestGenerator second = new ScheduledTestGenerator(config);
			second.counterMaximum = 2;
			second.generateTo = folder.getRoot();
			second.classNameTemplate = "${counter}SecondTest";
			second.dumpOnShutdown(true);

			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			testGenerator.accept(newSnapshot());
			second.accept(newSnapshot());
			second.accept(newSnapshot());
			second.accept(newSnapshot());
			assertThat(folder.fileNames()).isEmpty();

			Thread shutdown = shutdownHooks().entrySet().stream()
				.filter(e -> e.getKey().getName().equals("$generate-shutdown"))
				.map(e -> e.getValue())
				.findFirst().orElseThrow(() -> new AssertionError("no shutdown thread"));

			shutdown.run();
			shutdown.join();

			assertThat(folder.fileNames()).containsExactlyInAnyOrder("0Test.java", "0SecondTest.java");
		}
	}

	private ContextSnapshot contextSnapshot(Class<?> declaringClass, Type resultType, String methodName, Type... argumentTypes) {
		return new ContextSnapshot(0, "key", new VirtualMethodSignature(new MethodSignature(declaringClass, resultType, methodName, argumentTypes)));
	}

	private static int base = 8;

	private ContextSnapshot newSnapshot() {
		base++;

		ContextSnapshot snapshot = contextSnapshot(MyClass.class, int.class, "intMethod", int.class);
		FieldSignature field = new FieldSignature(MyClass.class, int.class, "field");
		snapshot.setSetupThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, base + 4))));
		snapshot.setSetupArgs(literal(int.class, base + 8));
		snapshot.setSetupGlobals(new SerializedField[0]);
		snapshot.setExpectThis(objectOf(MyClass.class, new SerializedField(field, literal(int.class, base))));
		snapshot.setExpectArgs(literal(int.class, base + 8));
		snapshot.setExpectResult(literal(int.class, base + 14));
		snapshot.setExpectGlobals(new SerializedField[0]);
		return snapshot;
	}

	private SerializedObject objectOf(Class<MyClass> type, SerializedField... fields) {
		SerializedObject setupThis = new SerializedObject(type);
		for (SerializedField field : fields) {
			setupThis.addField(field);
		}
		return setupThis;
	}

	private static Map<Thread, Thread> shutdownHooks() throws ClassNotFoundException {
		StaticShutdownHooks staticShutdownHooks = XRayInterface.xray(Class.forName("java.lang.ApplicationShutdownHooks"))
			.to(StaticShutdownHooks.class);

		return staticShutdownHooks.getHooks();
	}

	@SuppressWarnings("unused")
	private static class MyClass {

		private int field;

		public int intMethod(int arg) {
			return field + arg;
		}
	}

	interface StaticShutdownHooks {
		IdentityHashMap<Thread, Thread> getHooks();
	}

	interface OpenScheduledTestGenerator {
		void setDumpOnShutDown(Set<ScheduledTestGenerator> value);
	}
}
