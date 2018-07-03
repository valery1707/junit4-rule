package name.valery1707.junit.rule.utils;

import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Collections.unmodifiableMap;

@SuppressWarnings("WeakerAccess")
public class TestResult {
	private final Map<String, Description> completed;
	private final Map<String, Description> ignoredTotally;
	private final Map<String, Failure> ignoredByAssumption;
	private final Map<String, Failure> failures;

	private TestResult(
		Map<String, Description> completed,
		Map<String, Description> ignoredTotally,
		Map<String, Failure> ignoredByAssumption,
		Map<String, Failure> failures
	) {
		this.completed = completed;
		this.ignoredTotally = ignoredTotally;
		this.ignoredByAssumption = ignoredByAssumption;
		this.failures = failures;
	}

	public static TestResult runTest(Request request) {
		MyRunListener listener = new MyRunListener();

		Runner runner = request.getRunner();
		RunNotifier notifier = new RunNotifier();
		notifier.addFirstListener(listener);
		try {
			notifier.fireTestRunStarted(runner.getDescription());
			runner.run(notifier);
			notifier.fireTestRunFinished(null);
		} finally {
			notifier.removeListener(listener);
		}

		return new TestResult(
			unmodifiableMap(listener.completed),
			unmodifiableMap(listener.ignoredTotally),
			unmodifiableMap(listener.ignoredByAssumption),
			unmodifiableMap(listener.failures)
		);
	}

	public static TestResult runTest(Class<?> type) {
		return runTest(Request.aClass(type));
	}

	public static TestResult runTest(Class<?>... types) {
		return runTest(Request.classes(types));
	}

	public Map<String, Description> getCompleted() {
		return completed;
	}

	public Map<String, Description> getIgnoredTotally() {
		return ignoredTotally;
	}

	public Map<String, Failure> getIgnoredByAssumption() {
		return ignoredByAssumption;
	}

	public Map<String, Failure> getFailures() {
		return failures;
	}

	@Override
	public String toString() {
		return "TestResult{" +
			"completed=" + getCompleted() +
			", ignoredTotally=" + getIgnoredTotally() +
			", ignoredByAssumption=" + getIgnoredByAssumption() +
			", failures=" + getFailures() +
			'}';
	}

	private static class MyRunListener extends RunListener {
		private final AtomicLong timeRun = new AtomicLong();
		private final AtomicLong timeStart = new AtomicLong();
		private final ConcurrentHashMap<String, Description> completed = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<String, Failure> failures = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<String, Description> ignoredTotally = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<String, Failure> ignoredByAssumption = new ConcurrentHashMap<>();

		@Override
		public void testRunStarted(Description description) {
			timeStart.set(System.currentTimeMillis());
		}

		@Override
		public void testRunFinished(Result result) {
			timeRun.addAndGet(System.currentTimeMillis() - timeStart.get());
		}

		@Override
		public void testFinished(Description description) {
			completed.put(description.getMethodName(), description);
		}

		@Override
		public void testFailure(Failure failure) {
			String name = failure.getDescription().getMethodName();
			if (name == null) {
				String className = failure.getDescription().getClassName();
				if (className.contains("$")) {
					className = className.substring(className.lastIndexOf('$') + 1);
				} else {
					className = className.substring(className.lastIndexOf('.') + 1);
				}
				name = className;
			}
			failures.put(name, failure);
		}

		@Override
		public void testAssumptionFailure(Failure failure) {
			ignoredByAssumption.put(failure.getDescription().getMethodName(), failure);
		}

		@Override
		public void testIgnored(Description description) {
			ignoredTotally.put(description.getMethodName(), description);
		}
	}
}
