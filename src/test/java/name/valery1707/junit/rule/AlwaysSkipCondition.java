package name.valery1707.junit.rule;

import name.valery1707.junit.rule.ConditionalIgnoreRule.IgnoreCondition;

public class AlwaysSkipCondition implements IgnoreCondition {
	@Override
	public boolean needSkip() {
		return true;
	}
}
