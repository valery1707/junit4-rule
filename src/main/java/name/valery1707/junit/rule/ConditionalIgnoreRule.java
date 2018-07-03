package name.valery1707.junit.rule;

import org.junit.Assume;
import org.junit.rules.MethodRule;
import org.junit.runners.model.Annotatable;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Rule for ignore test by custom externalized predicates.
 * <p>
 * Class with {@link IgnoreCondition} implementation must be
 * <ul>
 * <li>or {@code static}: can be declared as in separate class and as internal class of test class</li>
 * <li>or {@code member}: must be declared as internal class of test class</li>
 * </ul>
 * <p>
 * For applying conditional ignoring you must follow the steps:
 * <ol>
 * <li>Create in test {@code public} field of class {@link ConditionalIgnoreRule} with annotation {@link org.junit.Rule}</li>
 * <li>Create class implementing {@link IgnoreCondition}</li>
 * <li>Add annotation {@link ConditionalIgnore} with needed condition implementation in {@link ConditionalIgnore#condition()}</li>
 * </ol>
 *
 * @see <a href="https://gist.github.com/rherrmann/7447571">Starting point</a>
 */
public class ConditionalIgnoreRule implements MethodRule {
	private static <A extends Annotation> Stream<A> annotation(Annotatable source, Class<A> type) {
		return Stream
			.of(source.getAnnotation(type))
			.filter(Objects::nonNull);
	}

	private static Stream<ConditionalIgnore> annotations(Annotatable source) {
		return Stream.concat(
			annotation(source, ConditionalIgnore.class),
			annotation(source, ConditionalIgnoreList.class).map(ConditionalIgnoreList::value).flatMap(Stream::of)
		);
	}

	@Override
	public Statement apply(Statement base, FrameworkMethod method, Object target) {
		return annotations(method)
			.map(annotation -> createCondition(target, annotation))
			.filter(IgnoreCondition::needSkip)
			.findFirst()
			.map(IgnoreStatement::new)
			.map(__ -> (Statement) __)
			.orElse(base);
	}

	@Nonnull
	private IgnoreCondition createCondition(Object target, ConditionalIgnore annotation) {
		Class<? extends IgnoreCondition> type = annotation.condition();
		boolean isStandalone = !type.isMemberClass() || Modifier.isStatic(type.getModifiers());
		boolean isDeclaredInTarget = target.getClass().isAssignableFrom(type.getDeclaringClass());
		if (!isStandalone && !isDeclaredInTarget) {
			String msg
				= "Conditional class '%s' is a member class "
				+ "but was not declared inside the test case using it.\n"
				+ "Either make this class a static class, "
				+ "standalone class (by declaring it in it's own file) "
				+ "or move it inside the test case using it";
			throw new IllegalArgumentException(String.format(msg, type.getName()));
		}
		try {
			if (isStandalone) {
				return type.newInstance();
			} else {
				return type.getDeclaredConstructor(target.getClass()).newInstance(target);
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static class IgnoreStatement extends Statement {
		private final IgnoreCondition condition;

		IgnoreStatement(IgnoreCondition condition) {
			this.condition = condition;
		}

		@Override
		public void evaluate() {
			String message = "Ignored by " + condition.getClass().getSimpleName();
			String reason = condition.reason();
			if (reason != null) {
				message += ": " + reason;
			}
			//This will skip test if boolean value is false
			Assume.assumeTrue(message, message.isEmpty()/*false*/);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	@Repeatable(ConditionalIgnoreList.class)
	public @interface ConditionalIgnore {
		/**
		 * Link to {@link IgnoreCondition} implementation.
		 *
		 * @return Class with {@link IgnoreCondition} implementation
		 */
		Class<? extends IgnoreCondition> condition();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD})
	@interface ConditionalIgnoreList {
		ConditionalIgnore[] value();
	}

	/**
	 * You <b>must</b> implement one of this methods:
	 * <ul>
	 * <li>{@link IgnoreCondition#needRun()} - allow to run test</li>
	 * <li>{@link IgnoreCondition#needSkip()} - force to skip test</li>
	 * </ul>
	 * <p>
	 * If desired, you can also implement the {@link IgnoreCondition#reason()} method for describe reason for skipping.
	 */
	public interface IgnoreCondition {
		/**
		 * Check to skip test.
		 *
		 * @return Is test need to skip?
		 */
		default boolean needSkip() {
			return !needRun();
		}

		/**
		 * Check to allow test to run.
		 *
		 * @return Is test need to run?
		 */
		default boolean needRun() {
			return !needSkip();
		}

		/**
		 * Description of skipping test.
		 *
		 * @return Description of skipping test
		 */
		@Nullable
		default String reason() {
			return null;
		}
	}
}
