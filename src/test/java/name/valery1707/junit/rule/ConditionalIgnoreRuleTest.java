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

import static name.valery1707.junit.rule.utils.TestResult.runTest;
import static org.assertj.core.api.Assertions.assertThat;

public class ConditionalIgnoreRuleTest {
	public static class AlwaysSkipCondition implements IgnoreCondition {
		@Override
		public boolean needSkip() {
			return true;
		}
	}

	public static class OnlySkipTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void test1() {
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void test2() {
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

	public static class AlwaysRunCondition implements IgnoreCondition {
		@Override
		public boolean needRun() {
			return true;
		}
	}

	public static class OnlyRunTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void test1() {
		}

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void test2() {
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

	public static class MixedTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		public void alwaysRun() {
		}

		@Test
		@ConditionalIgnore(condition = AlwaysRunCondition.class)
		public void runByRule() {
		}

		@Test
		@ConditionalIgnore(condition = AlwaysSkipCondition.class)
		public void skipByRule() {
		}

		@Test
		@Ignore
		public void alwaysSkip() {
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

	public static class SkipWithReasonCondition implements IgnoreCondition {
		static final String MESSAGE = "because I can";

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

	public static class IgnoreWithReasonTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		@Test
		@ConditionalIgnore(condition = SkipWithReasonCondition.class)
		public void test() {
		}
	}

	@Test
	public void testIgnoreWithReason() {
		TestResult result = runTest(IgnoreWithReasonTest.class);
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
		}
	}

	@Test
	public void testNonStaticIncorrect() {
		TestResult result = runTest(NonStaticIncorrectTest.class);
		assertThat(result.getFailures())
			.describedAs("failures")
			.isEmpty()
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
		//todo Catch exception
	}

	public static class NonStaticCorrectTest {
		private static final BigDecimal RND = new Random()
			.doubles()
			.limit(3)
			.mapToObj(BigDecimal::new)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(5, RoundingMode.CEILING);
		static final AtomicReference<BigDecimal> VALUE = new AtomicReference<>();
		private final BigDecimal rnd;

		public NonStaticCorrectTest() {
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
		}

		@Test
		public void executed() {
			assertThat(VALUE.compareAndSet(null, rnd)).isTrue();
		}
	}

	@Test
	public void testNonStaticCorrect() {
		TestResult result = runTest(NonStaticCorrectTest.class);
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
		assertThat(NonStaticCorrectTest.VALUE.get()).isNotNull();
		assertThat(result.getIgnoredByAssumption())
			.describedAs("ignoredByAssumption")
			.containsOnlyKeys("ignored")
			.hasEntrySatisfying("ignored", failure ->
				assertThat(failure.getMessage()).endsWith(NonStaticCorrectTest.VALUE.get().toEngineeringString())
			)
		;
	}
}
