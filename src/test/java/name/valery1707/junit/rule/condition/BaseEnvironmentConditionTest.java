package name.valery1707.junit.rule.condition;

import name.valery1707.junit.rule.ConditionalIgnoreRule;
import name.valery1707.junit.rule.utils.TestResult;
import org.junit.Rule;
import org.junit.Test;

import java.util.regex.Pattern;

import static name.valery1707.junit.rule.utils.TestResult.runTest;
import static org.assertj.core.api.Assertions.assertThat;

public class BaseEnvironmentConditionTest {
	//region NotExists
	public static class NotExistsTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		public static class NotExistsCondition extends BaseEnvironmentCondition {
			public NotExistsCondition() {
				super(StandardSource.Properties, "PreciselyUnknownValue", "Known");
			}
		}

		@Test
		@ConditionalIgnoreRule.ConditionalIgnore(condition = NotExistsCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testNotExists() {
		TestResult result = runTest(NotExistsTest.class);
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
				assertThat(failure.getMessage())
					.contains("PreciselyUnknownValue")
					.endsWith("don't have value")
			)
		;
	}
	//endregion

	//region StringEquals
	public static class StringEqualsTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		public static class StringEqualsCondition extends BaseEnvironmentCondition {
			public StringEqualsCondition() {
				super(StandardSource.Properties, "StringEqualsProperty", "Known");
			}
		}

		@Test
		@ConditionalIgnoreRule.ConditionalIgnore(condition = StringEqualsCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testStringEqualsNotEquals() {
		System.setProperty("StringEqualsProperty", "Unknown");
		TestResult result = runTest(StringEqualsTest.class);
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
				assertThat(failure.getMessage())
					.contains("StringEqualsProperty")
					.contains("Known")
			)
		;
	}

	@Test
	public void testStringEqualsEquals() {
		System.setProperty("StringEqualsProperty", "Known");
		TestResult result = runTest(StringEqualsTest.class);
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
			.isEmpty()
		;
	}
	//endregion

	//region Pattern
	public static class PatternTest {
		@Rule
		public ConditionalIgnoreRule rule = new ConditionalIgnoreRule();

		public static class PatternCondition extends BaseEnvironmentCondition {
			public PatternCondition() {
				super(StandardSource.Properties, "PatternProperty", Pattern.compile("^\\d+$"));
			}
		}

		@Test
		@ConditionalIgnoreRule.ConditionalIgnore(condition = PatternCondition.class)
		public void test() {
			assertThat("Blank").isNotBlank();
		}
	}

	@Test
	public void testPatternNotMatched() {
		System.setProperty("PatternProperty", "Unknown");
		TestResult result = runTest(PatternTest.class);
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
				assertThat(failure.getMessage())
					.contains("PatternProperty")
					.contains("^\\d+$")
			)
		;
	}

	@Test
	public void testPatternMatched() {
		System.setProperty("PatternProperty", "17");
		TestResult result = runTest(PatternTest.class);
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
			.isEmpty()
		;
	}
	//endregion
}
