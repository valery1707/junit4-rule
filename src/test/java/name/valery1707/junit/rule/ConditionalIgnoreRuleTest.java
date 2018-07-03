package name.valery1707.junit.rule;

import name.valery1707.junit.rule.ConditionalIgnoreRule.ConditionalIgnore;
import name.valery1707.junit.rule.ConditionalIgnoreRule.IgnoreCondition;
import name.valery1707.junit.rule.utils.TestResult;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static name.valery1707.junit.rule.ConditionalIgnoreRule.INVALID_CLASS_CTOR;
import static name.valery1707.junit.rule.ConditionalIgnoreRule.INVALID_CLASS_DECLARATION;
import static name.valery1707.junit.rule.utils.TestResult.runTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalIgnoreRuleTest {
	//region OnlySkip
	public static class OnlySkipTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void test1() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void test2() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testOnlySkip() {
		TestResult result = runTest(OnlySkipTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("test1", "test2")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("test1", "test2")
		;
	}
	//endregion

	//region OnlyRun
	public static class OnlyRunTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void test1() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void test2() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testOnlyRun() {
		TestResult result = runTest(OnlyRunTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("test1", "test2")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.isEmpty()
		;
	}
	//endregion

	//region Mixed
	public static class MixedTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		public void alwaysRun() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void runByRule() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void skipByRule() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@Ignore
		public void alwaysSkip() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		public void failure() {
			assertThat("Blank").isBlank();
		}
	}

	@Test
	public void testMixed() {
		TestResult result = runTest(MixedTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.containsOnlyKeys("failure")
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("alwaysRun", "runByRule", "skipByRule", "failure")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.containsOnlyKeys("alwaysSkip")
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("skipByRule")
		;
	}
	//endregion

	//region SkipWithReason
	public static class SkipWithReasonCondition implements IgnoreCondition {
		private static final String MESSAGE = "because I can";

		@Override
		public boolean needRun() {
			return false;
		}

		@Nullable
		@Override
		public String reason() {
			return MESSAGE;
		}
	}

	public static class SkipWithReasonTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = SkipWithReasonCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testSkipWithReason() {
		TestResult result = runTest(SkipWithReasonTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("test")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("test")
			.hasEntrySatisfying("test", failure ->
				assertThat(failure.getMessage()).endsWith(": " + SkipWithReasonCondition.MESSAGE)
			)
		;
	}
	//endregion

	//region NonStaticOuter
	public class NonStaticOuterCondition implements IgnoreCondition {
		@Override
		public boolean needSkip() {
			return true;
		}

		@Nullable
		@Override
		public String reason() {
			return ConditionalIgnoreRuleTest.this.toString();
		}
	}

	public static class NonStaticIncorrectTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = NonStaticOuterCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testNonStaticOuter() {
		TestResult result = runTest(NonStaticIncorrectTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.containsOnlyKeys("NonStaticIncorrectTest")
			.hasEntrySatisfying("NonStaticIncorrectTest", failure ->
				assertThat(failure.getMessage()).isEqualTo(
					String.format(INVALID_CLASS_DECLARATION, NonStaticOuterCondition.class.getName())
				)
			)
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.isEmpty()
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.isEmpty()
		;
	}
	//endregion

	//region NonStaticInner
	public static class NonStaticInnerTest {
		private static final BigDecimal RND = new Random()
			.doubles()
			.limit(3)
			.mapToObj(BigDecimal::new)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(5, RoundingMode.CEILING);
		private static final AtomicReference<BigDecimal> VALUE = new AtomicReference<>();
		private final BigDecimal rnd;

		public NonStaticInnerTest() {
			this.rnd = RND;
		}

		public class NonStaticCondition implements IgnoreCondition {
			@Override
			public boolean needSkip() {
				return true;
			}

			@Nullable
			@Override
			public String reason() {
				return "inner value = " + rnd.toEngineeringString();
			}
		}

		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = NonStaticCondition.class)
		public void ignored() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		public void executed() {
			assertThat(VALUE.compareAndSet(null, rnd)).isTrue();
		}
	}

	@Test
	public void testNonStaticInner() {
		TestResult result = runTest(NonStaticInnerTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("executed", "ignored")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(NonStaticInnerTest.VALUE.get()).isNotNull();
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("ignored")
			.hasEntrySatisfying("ignored", failure ->
				assertThat(failure.getMessage()).endsWith(NonStaticInnerTest.VALUE.get().toEngineeringString())
			)
		;
	}
	//endregion

	//region Repeatable
	public static class RepeatableTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void runAndRun() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void runAndSkip() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void skipAndRun() {
			assertThat("Blank").isNotBlank();
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void skipAndSkip() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testRepeatable() {
		TestResult result = runTest(RepeatableTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.containsOnlyKeys("runAndRun", "runAndSkip", "skipAndRun", "skipAndSkip")
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("runAndSkip", "skipAndRun", "skipAndSkip")
		;
	}
	//endregion

	//region ConstructorWithError
	public static class ConstructorWithErrorCondition implements IgnoreCondition {
		public ConstructorWithErrorCondition() {
			throw new IllegalStateException("Some unchecked exception");
		}

		@Override
		public boolean needRun() {
			return true;
		}
	}

	public static class ConstructorWithErrorTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = ConstructorWithErrorCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testConstructorWithError() {
		TestResult result = runTest(ConstructorWithErrorTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.containsOnlyKeys("ConstructorWithErrorTest")
			.hasEntrySatisfying("ConstructorWithErrorTest", failure ->
				assertThat(failure.getMessage()).isEqualTo("Some unchecked exception")
			)
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.isEmpty()
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.isEmpty()
		;
	}
	//endregion

	//region ConstructorHidden
	public static class ConstructorHiddenCondition implements IgnoreCondition {
		private ConstructorHiddenCondition() {
		}

		@Override
		public boolean needRun() {
			return true;
		}
	}

	public static class ConstructorHiddenTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = ConstructorHiddenCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testConstructorHidden() {
		TestResult result = runTest(ConstructorHiddenTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.containsOnlyKeys("ConstructorHiddenTest")
			.hasEntrySatisfying("ConstructorHiddenTest", failure ->
				assertThat(failure.getMessage()).isEqualTo(
					String.format(INVALID_CLASS_CTOR, ConstructorHiddenCondition.class.getName())
				)
			)
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.isEmpty()
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.isEmpty()
		;
	}
	//endregion

	//region ConstructorWithArguments
	public static class ConstructorWithArgumentsCondition implements IgnoreCondition {
		private final String value;

		public ConstructorWithArgumentsCondition(String value) {
			this.value = value;
		}

		@Override
		public boolean needRun() {
			return value != null;
		}
	}

	public static class ConstructorWithArgumentsTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = ConstructorWithArgumentsCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testConstructorWithArguments() {
		TestResult result = runTest(ConstructorWithArgumentsTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.containsOnlyKeys("ConstructorWithArgumentsTest")
			.hasEntrySatisfying("ConstructorWithArgumentsTest", failure ->
				assertThat(failure.getMessage()).isEqualTo(
					String.format(INVALID_CLASS_CTOR, ConstructorWithArgumentsCondition.class.getName())
				)
			)
		;
		assertThat(result.getCompleted())
			.describedAs("completed")
			.isEmpty()
		;
		assertThat(result.getIgnoredTotally())
			.describedAs("ignoredTotally")
			.isEmpty()
		;
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.isEmpty()
		;
	}
	//endregion
}
