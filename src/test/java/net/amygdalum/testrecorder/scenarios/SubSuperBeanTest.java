package net.amygdalum.testrecorder.scenarios;

import static com.almondtools.conmatch.strings.WildcardStringMatcher.containsPattern;
import static net.amygdalum.testrecorder.dynamiccompile.CompilableMatcher.compiles;
import static net.amygdalum.testrecorder.dynamiccompile.TestsRunnableMatcher.testsRun;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.amygdalum.testrecorder.TestGenerator;
import net.amygdalum.testrecorder.util.Instrumented;
import net.amygdalum.testrecorder.util.TestRecorderAgentExtension;

@ExtendWith(TestRecorderAgentExtension.class)
@Instrumented(classes={"net.amygdalum.testrecorder.scenarios.SuperBean", "net.amygdalum.testrecorder.scenarios.SubBean"})
public class SubSuperBeanTest {

	

	@Test
	public void testCompilable() throws Exception {
		SubBean bean = new SubBean();
		bean.setI(22);
		bean.setO(new SubBean());

		assertThat(bean.hashCode()).isEqualTo(191);

		TestGenerator testGenerator = TestGenerator.fromRecorded();
		assertThat(testGenerator.renderTest(SubBean.class), compiles(SubBean.class));
		assertThat(testGenerator.renderTest(SubBean.class), testsRun(SubBean.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCode() throws Exception {
		SubBean bean = new SubBean();
		bean.setI(22);
		bean.setO(new SubBean());

		assertThat(bean.hashCode()).isEqualTo(191);

		TestGenerator testGenerator = TestGenerator.fromRecorded();
		assertThat(testGenerator.testsFor(SubBean.class)).hasSize(2);
		assertThat(testGenerator.testsFor(SubBean.class), containsInAnyOrder(
			allOf(containsString("new SubBean()"), not(containsPattern("subBean?.set")), containsString("equalTo(13)")),
			allOf(containsPattern("subBean?.setI(22)"), containsPattern("subBean?.setO(subBean?)"), containsString("equalTo(191)"))));
	}
}