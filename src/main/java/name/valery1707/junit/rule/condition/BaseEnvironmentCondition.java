package name.valery1707.junit.rule.condition;

import name.valery1707.junit.rule.ConditionalIgnoreRule.IgnoreCondition;
import org.intellij.lang.annotations.MagicConstant;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Test of environment values by regexp or simple equals.
 */
@SuppressWarnings("WeakerAccess")
public class BaseEnvironmentCondition implements IgnoreCondition {
	private final Source source;
	private final String name;
	private final Pattern pattern;

	/**
	 * Value extractor.
	 */
	public interface Source {
		/**
		 * Extract value with {@code name}.
		 *
		 * @param name Name of the property
		 * @return Value if exists or Empty
		 */
		Optional<String> find(String name);
	}

	/**
	 * Constant definitions for the standard {@link Source}.
	 */
	public enum StandardSource implements Source {
		Environment(System::getenv),
		Properties(System::getProperty),
		//Formatting
		;

		private final Function<String, Optional<String>> extractor;

		StandardSource(Function<String, String> extractor) {
			this.extractor = extractor.andThen(Optional::ofNullable);
		}

		@Override
		public Optional<String> find(String name) {
			return extractor.apply(name);
		}
	}

	/**
	 * Test value with pattern.
	 *
	 * @param source  Value source
	 * @param name    Property name
	 * @param pattern Patter for test
	 */
	protected BaseEnvironmentCondition(Source source, String name, Pattern pattern) {
		this.source = source;
		this.name = name;
		this.pattern = pattern;
	}

	/**
	 * Test value by equals.
	 *
	 * @param source   Value source
	 * @param name     Property name
	 * @param expected Expected value
	 * @param flags    Pattern flags
	 * @see Pattern#compile(String, int)
	 */
	protected BaseEnvironmentCondition(
		Source source, String name, String expected,
		@MagicConstant(flagsFromClass = Pattern.class) int flags
	) {
		this(source, name, Pattern.compile("^" + Pattern.quote(expected) + "$", flags));
	}

	/**
	 * Test value by equals.
	 *
	 * @param source   Value source
	 * @param name     Property name
	 * @param expected Expected value
	 */
	protected BaseEnvironmentCondition(Source source, String name, String expected) {
		this(source, name, expected, 0);
	}

	@Override
	public boolean needRun() {
		return source
			.find(name)
			.filter(pattern.asPredicate())
			.isPresent();
	}

	@Nullable
	@Override
	public String reason() {
		return source
			.find(name)
			.map(value ->
				String.format("Key '%s' has value '%s' which don't match with pattern '%s'", name, value, pattern.pattern())
			)
			.orElseGet(() ->
				String.format("Key '%s' don't have value", name)
			);
	}
}
